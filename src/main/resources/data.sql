-- tax_company 더미 (ID를 명시로 박음: identity가 BY DEFAULT라 가능)
INSERT INTO tax_company (cpa_id, name)
VALUES
  (1, 'QuickTax Demo CPA 1'),
  (2, 'QuickTax Demo CPA 2')
ON CONFLICT (cpa_id) DO NOTHING;

-- customer 더미
INSERT INTO customer (customer_id, cpa_id, name, rrn_enc, bank_code, account_number_enc)
VALUES
  (1, 1, '홍길동', 'RRN_ENC_DUMMY_1', 'KB', 'ACCT_ENC_DUMMY_1'),
  (2, 1, '김철수', 'RRN_ENC_DUMMY_2', 'SHINHAN', 'ACCT_ENC_DUMMY_2'),
  (3, 2, '이영희', 'RRN_ENC_DUMMY_3', 'WOORI', 'ACCT_ENC_DUMMY_3')
ON CONFLICT (customer_id) DO NOTHING;
