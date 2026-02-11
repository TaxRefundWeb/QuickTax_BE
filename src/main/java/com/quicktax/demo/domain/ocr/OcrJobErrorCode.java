package com.quicktax.demo.domain.ocr;

/**
 * ocr_job.error_code에 들어가는 “업무용 에러 코드”.
 * (common.ErrorCode랑 별개)
 */
public enum OcrJobErrorCode {
    /** 업로드 완료(/complete) 시점에 S3에 원본이 없어서 처리 시작을 못 함 */
    UPLOAD_NOT_FOUND,

    /** /complete 단계에서 S3 HEAD가 403/5xx/네트워크 오류 등으로 실패 */
    S3_HEAD_ERROR,

    /** /complete 단계에서 SQS 전송 실패 */
    QUEUE_SEND_ERROR,

    /** 워커에서 예외 발생 */
    WORKER_EXCEPTION,

    /** 워커 타임아웃(워치독) */
    WORKER_TIMEOUT
}
