package com.example.payments.adapter.persistence.repository

import com.example.payments.adapter.persistence.model.AccountTableModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface AccountRepository : ReactiveCrudRepository<AccountTableModel, Long>
