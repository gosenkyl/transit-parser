package com.gosenk.transit.parser;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.StringJoiner;

@Component
public class Main implements CommandLineRunner {

    private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> structureMap;

    @SuppressWarnings("unchecked")
    public Main(){
        Yaml yaml = new Yaml();
        InputStream yml = getClass().getResourceAsStream("/data_structure.yml");
        structureMap = (LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>) yaml.load(yml);
    }

    @Override
    public void run(String... args) throws Exception {

        // Lansing CATA
        InputStream is = getClass().getResourceAsStream("/google_transit");
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data.sql"), "utf-8"));
        writer.write("use msu_cata;");

        String inputLine;
        while((inputLine = in.readLine()) != null){
            InputStream fileStream = getClass().getResourceAsStream("/google_transit/"+inputLine);
            generateInserts(fileStream, writer, inputLine);
        }

        in.close();
        writer.close();
    }

    private void generateInserts(InputStream is, Writer writer, String fileName) throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String firstLine = in.readLine();

        if(firstLine == null){
            throw new Exception("No Data Found");
        }

        String tableName = fileName.split("\\.")[0];

        LinkedHashMap<String, LinkedHashMap<String, String>> propertyDefs = structureMap.get(tableName);

        // Reads the first line of the file (columns) for insert order
        StringJoiner sj = new StringJoiner(" ");
        sj.add("INSERT INTO");
        sj.add(tableName);
        sj.add("(");

        String[] fields = firstLine.split(",", -1);

        int i = 0;
        String name;
        LinkedHashMap<String, String> typeNameMap;
        for(String field : fields){
            if(i > 0){
                sj.add(",");
            }

            typeNameMap = propertyDefs != null ? propertyDefs.get(field) : null;
            name = typeNameMap != null && typeNameMap.get("name") != null ? typeNameMap.get("name") : field;

            sj.add(name);
            i++;
        }

        sj.add(") VALUES (");

        String initStr = sj.toString();

        String inputLine;
        while((inputLine = in.readLine()) != null){
            String[] values = inputLine.split(",", -1);

            sj = new StringJoiner(" ");
            sj.add(initStr);

            int j = 0;
            String type;
            for(String value : values){
                typeNameMap = propertyDefs != null ? propertyDefs.get(fields[j]) : null;
                type = typeNameMap != null && typeNameMap.get("type") != null ? typeNameMap.get("type") : "string";

                if(j > 0){
                    sj.add(",");
                }
                sj.add(getFieldValue(value, type));
                j++;
            }

            sj.add(");");

            writer.write(sj.toString());
            System.out.println(sj.toString());
        }
        is.close();
    }

    private String getFieldValue(String value, String type) throws Exception{
        if(StringUtils.isEmpty(value)){
            return "NULL";
        }

        if(type == null){
            type =  "string";
        }

        String result = "''";
        switch(type){
            case "string":
                result = "'" + value.replace("'", "''") + "'";
                break;
            case "time":
                result = "'" + value + "'";
                break;
            case "long":
            case "int":
                result = value;
                break;
            case "date":
                result = "'" + formatDate(value) + "'";
                //result = String.valueOf(dateToMilliseconds(value));
                break;
            case "bool":
                result = value;
                break;
            default:
                break;
        }

        return result;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

    private String formatDate(String dateStr) throws Exception{
        return sdf2.format(sdf.parse(dateStr));
    }

   /* private long dateToMilliseconds(String dateStr) throws Exception{
        return sdf.parse(dateStr).getTime();
    }*/
}
