package com.quicktax.demo.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsOcrConsumer {

    private final SqsClient sqs;
    private final ObjectMapper objectMapper;
    private final OcrPipelineService ocrPipelineService;

    @Value("${app.sqs.queue-url}")
    private String queueUrl;

    // 3초마다 폴링. long polling(WaitTimeSeconds=20)이라 실제 호출은 덜 난다.
    @Scheduled(fixedDelayString = "${app.sqs.poll-delay-ms:3000}")
    public void poll() {
        ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(5)
                .waitTimeSeconds(20) // long polling
                .visibilityTimeout(120) // 처리 시간보다 넉넉히
                .build();

        List<Message> messages = sqs.receiveMessage(req).messages();
        if (messages.isEmpty()) return;

        for (Message m : messages) {
            try {
                OcrStartMessage msg = objectMapper.readValue(m.body(), OcrStartMessage.class);

                // 타입 방어
                if (!"OCR_START".equals(msg.getType())) {
                    log.warn("skip unknown type: {}", msg.getType());
                    delete(m);
                    continue;
                }

                ocrPipelineService.handle(msg); // 여기서 PDF 분할 + CLOVA OCR + S3 저장 + DB 업데이트 + READY/FAILED

                delete(m); // 성공한 것만 delete (중요)
            } catch (Exception e) {
                log.error("SQS message 처리 실패. will retry. body={}", m.body(), e);
                // delete 안 하면 visibility timeout 지나고 다시 Visible로 돌아와 재시도됨
            }
        }
    }

    private void delete(Message m) {
        sqs.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(m.receiptHandle())
                .build());
    }
}
