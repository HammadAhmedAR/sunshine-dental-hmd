-- Report source shared by billing summaries. Issued revenue is not payment collection.
BEGIN;
CREATE VIEW billing_revenue_summary AS
SELECT (issued_at AT TIME ZONE 'Asia/Colombo')::DATE AS revenue_date,
       count(*) AS bill_count,
       sum(treatment_cost) AS treatment_revenue,
       sum(consultation_fee) AS consultation_revenue,
       sum(discount) AS discounts,
       sum(total) AS total_revenue
FROM bills GROUP BY (issued_at AT TIME ZONE 'Asia/Colombo')::DATE;
-- Supports the report view's local-date predicate; issued_at index separately serves ordered history.
CREATE INDEX idx_bills_local_date ON bills (((issued_at AT TIME ZONE 'Asia/Colombo')::DATE));
COMMIT;
