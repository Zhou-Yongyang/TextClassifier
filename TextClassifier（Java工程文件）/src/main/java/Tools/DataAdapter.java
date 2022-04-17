package Tools;

import java.io.*;
import java.util.List;

//数据适配器，用来读入单个文件的所有内容，转化为String对象
public class DataAdapter {
    public static String readData(String filePath, String encoding)
    {
        String data = "";
        if(filePath == null || filePath.isEmpty())
        {
            System.out.println("None path");
            return null;
        }
        File file = new File(filePath);
        try
        {
            if(file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                String lineTxt;
                BufferedReader bufferedReader = new BufferedReader(read);
                while ((lineTxt = bufferedReader.readLine()) != null)
                {
                    data += lineTxt;
                }
                bufferedReader.close();
                read.close();
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return data;
    }
    public static String readData(File file, String encoding)
    {
        StringBuilder data = new StringBuilder();
        try
        {
            if(file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                String lineTxt;
                BufferedReader bufferedReader = new BufferedReader(read);
                while ((lineTxt = bufferedReader.readLine()) != null)
                {
                   data.append(lineTxt);
                }
                bufferedReader.close();
                read.close();
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return data.toString();
    }
    public static void writeData(List<String> dataList, String filePath)
    {
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                new FileOutputStream(filePath))) {
            for (String str : dataList) {
                str += "\n";
                bufferedOutputStream.write(str.getBytes());
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }
}
