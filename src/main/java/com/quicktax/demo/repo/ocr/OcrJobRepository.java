package com.quicktax.demo.repo.ocr;

import com.quicktax.demo.domain.ocr.OcrJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OcrJobRepository extends JpaRepository<OcrJob, Long> {
}
