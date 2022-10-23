import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SqlMapping {
    //엑셀에 있는 모든 테이블과 컬럼들을 저장
    private static HashMap<String, TableEntity> tableMap=new HashMap<>();
    
    //sql를 단어로 쪼갠 리스트
    private static List<String> words = new ArrayList<>();
    
    //sql이 사용하고 있는 테이블과 컬럼들을 저장
    private static HashMap<String, TableEntity> sqlContainsTableMap = new HashMap<>();


    public static void main(String[] args) {
        //메모장에서 sql 추출
        String sql = getSqlByTxt();
        //extractWord에서 마지막 단어를 추출하기 위해서 한 인덱스를 추가
        sql+=";";

        //메모장에서 추출한 sql를 단어로 분리해서 리스트에 저장
        extractWord(sql);

        //엑셀에서 매핑 정보 가져오기
        List<String> excelList = getTableColumnMappingString();

        //엑셀에서 가져온 매핑 정보를 tableEntityMap에 세팅하기
        setSqlMappingMap(excelList);

        //sql에 존재하는 테이블을 sqlContainsTableMap에 따로 저장
        findTableEntityAndInsert(words);

        //매핑 시작
        String mappedSql = executeMapping(sql, sqlContainsTableMap);

        //메모장에 매핑한 sql 저장
        saveTxtFile(mappedSql);
    }

    public static void saveTxtFile(String sql){
        String text = sql;
        String fileNm = "C:/sql-mapping/mapped-sql.txt";

        try{
            File file = new File(fileNm);

            //경로에 똑같은 파일이 존재하면 삭제하고 다시 만들기
            if(file.exists()){
                file.delete();
            }

            FileWriter fileWrite = new FileWriter(file, true);
            fileWrite.write(text);
            fileWrite.flush();
            fileWrite.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String executeMapping(String sql, HashMap<String, TableEntity> map){
        String mappedSql = sql;
        TableEntity tableEntity = null;
        for(int i=0;i<words.size() ;i++){
            //sql 안에 존재하는 테이블을 각각 꺼내면서 그 테이블에 해당하는 컬럼이 있다면 매핑한다.
            for(String strKey : map.keySet()){
                tableEntity = map.get(strKey);
                //컬럼명 매핑
                if(tableEntity.getColumnMappingMap().containsKey(words.get(i))){
                    mappedSql = mappedSql.replaceAll(words.get(i),tableEntity.getColumnMappingMap().get(words.get(i)).getToBeLogicalColName());
                }

                //테이블명 매핑
                if(tableEntity.getAsIsTableName().equals(words.get(i))){
                    mappedSql = mappedSql.replaceAll(words.get(i), tableEntity.getToBeTableName());
                }
            }
        }
        return mappedSql;
    }

    public static void findTableEntityAndInsert(List<String> words){
        for(String word : words){
            if(tableMap.containsKey(word.trim())){
                //sql에 존재하는 테이블을 sqlContainsTableMap에 따로 저장
                sqlContainsTableMap.put(tableMap.get(word).getAsIsTableName(), tableMap.get(word));
            }
        }
    }

    public static List<String> extractWord(String sql){
        String word = "";

        for(int i=0;i<sql.length();i++){
            if(Character.isLetter(sql.charAt(i)) || sql.charAt(i) == 45 || Character.isDigit(sql.charAt(i))){
                word+=Character.toString(sql.charAt(i));
            }else{
                if(word.length() > 1)
                    words.add(word.toUpperCase());

                word = "";
            }
        }

        return words;
    }

    public static void setSqlMappingMap(List<String> excelList){
        for(String str:excelList){
            TableEntity tableEntity = new TableEntity();
            String[] cell = str.split(" ");

            //To-Be 테이블 설정
            tableEntity.setToBeTableName(cell[0]);
            //As-Is 테이블 설정
            tableEntity.setAsIsTableName(cell[3]);

            //To-Be, As-Is 컬럼 설정
            //key는 As-Is 컬럼명
            String key = cell[4];

            //value는 value로 As-Is 컬럼의 논리명과 물리명 그리고 To-Be 컬럼의 논리명과 물리명
            ColumnEntity value = new ColumnEntity();
            //To-Be 컬럼 논리명
            value.setToBeLogicalColName(cell[1]);
            //To-Be 컬럼 물리명
            value.setToBePhysicalColName(cell[2]);
            //As-Is 컬럼 논리명
            value.setAsIsLogicalColName(cell[4]);
            //As-Is 컬럼 물리명
            value.setAsIsPhysicalColName(cell[5]);

            //TableMap 값 세팅
            if(!tableMap.containsKey(tableEntity.getAsIsTableName())){
                tableEntity.insertMap(key, value);
                tableMap.put(tableEntity.getAsIsTableName(), tableEntity);
            }
            else{
                tableMap.get(tableEntity.getAsIsTableName()).insertMap(key, value);
            }

         //   printHashMap(tableEntity.getColumnMappingMap());
        }
    }

    //메모장에서 sql 내용 가져오기
    public static String getSqlByTxt(){
        File note = new File("C:/sql-mapping/sql.txt");
        BufferedReader br = null;
        String sql = "";
        try {
            br = new BufferedReader(new FileReader(note));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                line = line.trim();
                line+=" \r\n";
                sql += line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sql.toUpperCase();
    }

    public static List<String> getTableColumnMappingString(){
        List<String> excelList = new ArrayList<>();
        try {
            // 경로에 있는 파일을 읽
            FileInputStream file = new FileInputStream("C:/sql-mapping/sql-excel.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            int rowNo = 0;
            int cellIndex = 0;

            XSSFSheet sheet = workbook.getSheetAt(0); // 0 번째 시트를 가져온다
            // 만약 시트가 여러개 인 경우 for 문을 이용하여 각각의 시트를 가져온다
            int rows = sheet.getPhysicalNumberOfRows(); // 사용자가 입력한 엑셀 Row수를 가져온다
            for(rowNo = 0; rowNo < rows; rowNo++){
                String rowStr = "";
                XSSFRow row = sheet.getRow(rowNo);
                if(row != null){
                    int cells = row.getPhysicalNumberOfCells(); // 해당 Row에 사용자가 입력한 셀의 수를 가져온다
                    for(cellIndex = 0; cellIndex <= cells; cellIndex++){
                        XSSFCell cell = row.getCell(cellIndex); // 셀의 값을 가져온다
                        String value = "";
                        if(cell == null){ // 빈 셀 체크
                            continue;
                        }else{
                            // 타입 별로 내용을 읽는다
                            switch (cell.getCellType()){
                                case NUMERIC:
                                    value = cell.getNumericCellValue() + "";
                                    break;
                                case STRING:
                                    value = cell.getStringCellValue() + "";
                                    break;
                                case BLANK:
                                    value = cell.getBooleanCellValue() + "";
                                    break;
                                case ERROR:
                                    value = cell.getErrorCellValue() + "";
                                    break;
                            }
                        }
                        rowStr += value+" ";
                    }
                }
                excelList.add(rowStr);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return excelList;
    }

    public static void printList(List<String> list){
        for (int i=0;i<list.size();i++){
            System.out.println(list.get(i));
        }
    }

    public static void printHashMapColumnEntity(HashMap<String, ColumnEntity> map){
        for( String strKey : map.keySet() ){
            ColumnEntity object = map.get(strKey);
            System.out.println("key:"+ strKey );
            System.out.println(object.getToBeLogicalColName()+" "+object.getToBePhysicalColName()
            +" "+object.getAsIsLogicalColName()+" "+object.getAsIsPhysicalColName());
        }
    }
    public static void printHashMapTableEntity(HashMap<String, TableEntity> map){
        for( String strKey : map.keySet() ){
            TableEntity object = map.get(strKey);
            System.out.println("key:"+ strKey +" value:"+object.getAsIsTableName());
        }
    }
}
