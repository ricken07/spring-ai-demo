package fr.sciam.springai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Map;
import java.util.function.Function;

@Configuration
public class FunctionsCalling {
    private static final Logger log = LoggerFactory.getLogger(FunctionsCalling.class);

    record Transaction(String id){}

    record TransactionStatus(String status){}

    private static final Map<Transaction, TransactionStatus> DATASET = Map.of(
            new Transaction("001"), new TransactionStatus("pending"),
            new Transaction("002"), new TransactionStatus("approved"),
            new Transaction("003"), new TransactionStatus("rejected"));

    @Bean
    @Description("Get the status of a payment transaction")
    public Function<Transaction, TransactionStatus> paymentStatus() {
        return DATASET::get;
    }
}
