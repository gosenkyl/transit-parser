package com.gosenk.transit.parser;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.text.SimpleDateFormat;
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

        String inputLine;
        while((inputLine = in.readLine()) != null){
            if(inputLine.contains(".txt")) {
                InputStream fileStream = getClass().getResourceAsStream("/google_transit/" + inputLine);
                generateInserts(fileStream, inputLine);
            }
        }

        in.close();
    }

    private void generateInserts(InputStream is, String fileName) throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String firstLine = in.readLine();

        if(firstLine == null){
            throw new Exception("No Data Found");
        }

        String tableName = fileName.split("\\.")[0];

        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/" + tableName + ".sql"), "utf-8"));

        LinkedHashMap<String, LinkedHashMap<String, String>> propertyDefs = structureMap.get(tableName);

        // Reads the first line of the file (columns) for insert order
        StringBuilder sb = new StringBuilder();

        sb.append("USE transit;");

        sb.append("TRUNCATE TABLE ");
        sb.append(tableName);
        sb.append(";");

        sb.append("INSERT INTO ");
        sb.append(tableName);
        sb.append(" (");

        String[] fields = firstLine.split(",", -1);

        int i = 0;
        String name;
        LinkedHashMap<String, String> typeNameMap;
        for(String field : fields){
            if(i > 0){
                sb.append(",");
            }

            typeNameMap = propertyDefs != null ? propertyDefs.get(field) : null;
            name = typeNameMap != null && typeNameMap.get("name") != null ? typeNameMap.get("name") : field;

            sb.append(name);
            i++;
        }

        sb.append(") VALUES ");

        String insertPrefix = sb.toString();
        writer.write(insertPrefix);
        //System.out.println(sb.toString());

        int count = 0;
        int fileNumber = 0;
        String inputLine;
        while((inputLine = in.readLine()) != null){
            String[] values = inputLine.split(",", -1);

            sb.setLength(0);

            if(count > 0){
                sb.append(",");
            }

            sb.append("(");

            int j = 0;
            String type;
            for(String value : values){
                typeNameMap = propertyDefs != null ? propertyDefs.get(fields[j]) : null;
                type = typeNameMap != null && typeNameMap.get("type") != null ? typeNameMap.get("type") : "string";

                if(j > 0){
                    sb.append(",");
                }
                sb.append(getFieldValue(value, type));
                j++;
            }

            sb.append(")");

            writer.write(sb.toString());
            //System.out.println(sb.toString());

            count++;

            if(count > 50000){
                writer.write(";");

                writer.close();

                fileNumber++;
                count = 0;

                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/" + tableName + fileNumber + ".sql"), "utf-8"));
                writer.write(insertPrefix.replace("TRUNCATE TABLE " + tableName + ";", ""));
            }
        }

        if(count > 0) {
            writer.write(";");
        }

        is.close();
        writer.close();

        System.out.println("----- " + tableName + " DONE -----");
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
