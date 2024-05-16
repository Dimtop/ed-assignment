package com.dterz.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import com.dterz.dtos.TransactionDTO;

@Component
public class TransactionsCsvConverter extends AbstractGenericHttpMessageConverter<Map<String, Object>> {

    public TransactionsCsvConverter() {
        super(new MediaType("text", "csv"));
    }

    protected boolean supports(Class<?> clazz) {
        return HashMap.class.equals(clazz);
    }

    @Override
    protected void writeInternal(
            Map<String, Object> hashMap, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        outputMessage.getHeaders().setContentType(new MediaType("text", "csv"));
        outputMessage.getHeaders().set("Content-Disposition", "attachment; filename=transactions.csv");
        OutputStream outputStream = outputMessage.getBody();
        List<TransactionDTO> transactions = (List<TransactionDTO>) hashMap.get("transactions");
        String header = "id,amount,date,descripton,type,user_name,account_name\n";

        outputStream.write(header.getBytes(Charset.forName("utf-8")));
        for (TransactionDTO transaction : transactions) {
            ArrayList<String> values = new ArrayList<String>();
            String row;
            values.add(Long.toString(transaction.getId()));
            values.add(transaction.getAmount().toString());
            values.add(transaction.getDate().toString());
            values.add(transaction.getDescription());
            values.add(transaction.getType().toString());
            values.add(transaction.getUserName());
            values.add(transaction.getAccountName());

            row = String.join(",", values);
            row += '\n';

            outputStream.write(row.getBytes(Charset.forName("utf-8")));

        }

        outputStream.close();

    }

    @Override
    protected Map<String, Object> readInternal(Class<? extends Map<String, Object>> clazz,
            HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    public Map<String, Object> read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return null;
    }

}
