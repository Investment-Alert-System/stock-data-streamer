package com.microservice.stockdatastreamer.core;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.microservice.stockdatastreamer.config.KafkaConfig;
import com.microservice.stockdatastreamer.exception.DataValidationException;
import com.microservice.stockdatastreamer.producer.StockDataProducer;
import com.microservice.stockdatastreamer.stream.APIDataConsumer;
import com.microservice.stockdatastreamer.validate.Validator;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

public class DataHandler {

    private final RestTemplateBuilder restTemplateBuilder;

    public DataHandler(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }


    public void fetchAndValidateData() throws DataValidationException {
        APIDataConsumer apiDataConsumer = new APIDataConsumer(restTemplateBuilder);
        String data = apiDataConsumer.fetchData();
        Validator validator = new Validator();
        try {
            ProcessingReport APIDataProcessingReport =  validator.validateResponseMetaData(data);

            System.out.println("APIDataProcessingReport: " + APIDataProcessingReport);
            if (APIDataProcessingReport.isSuccess()) {
                System.out.println("Data Validation Successful");
                KafkaConfig kafkaConfig = new KafkaConfig();
                StockDataProducer stockDataProducer = new StockDataProducer(kafkaConfig.kafkaTemplate());
                stockDataProducer.sendValidData(data);
            } else {
                throw new DataValidationException("Data Validation Failed");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while validating data", e.getCause());
        }
    }

}
