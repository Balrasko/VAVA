package dev.vavateam1.service;

import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.dto.PaymentDto;

public class HistoryServiceImpl implements HistoryService {

    private final PaymentDao paymentDao;

    @Inject
    public HistoryServiceImpl(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    @Override
    public List<PaymentDto> getPayments() {
        return paymentDao.findAll();
    }
}