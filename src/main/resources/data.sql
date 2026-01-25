-- 이건 나중에 지울 테이블값들임 개발중엔 그냥 예시로써 확인하기 위해 들고 다니자
-- tax_company 예
INSERT INTO tax_company (cpa_id, password)
VALUES
  (1, '$2a$10$dummyhashdummyhashdummyhashdummyhashdummyhashdummyhash'),
  (2, '$2a$10$FwsNz9ZIPeDePsZlRXagk.mz1EgNwjXZ1SCLMHi3cmTIDQPZDPQMa') -- 비밀번호 1234
ON CONFLICT (cpa_id)
DO UPDATE SET password = EXCLUDED.password;


INSERT INTO customer (name, address, rrn, bank, bank_number, nationality_code, nationality_name, final_fee_percent, cpa_id)
VALUES
  ('김민수', '서울특별시 마포구 월드컵북로 123', '900101-1234567', 'KB', '110-123-456789', 'KOR', 'Korea, Republic of', 15, 1),
  ('박지은', '서울특별시 강남구 테헤란로 45', '920305-2345678', 'NH', '352-987-654321', 'KOR', 'Korea, Republic of', 12, 1),
  ('이도현', '부산광역시 해운대구 센텀동로 77', '880712-3456789', 'SH', '123-456-789012', 'KOR', 'Korea, Republic of', 10, 1),
  ('최서연', '대구광역시 수성구 동대구로 10', '950228-4567890', 'KAKAO', '3333-12-9876543', 'KOR', 'Korea, Republic of', 18, 1),
  ('정우진', '인천광역시 연수구 송도과학로 88', '870915-5678901', 'WOORI', '1002-345-678901', 'KOR', 'Korea, Republic of', 20, 1)
ON CONFLICT (cpa_id, rrn) DO NOTHING;
