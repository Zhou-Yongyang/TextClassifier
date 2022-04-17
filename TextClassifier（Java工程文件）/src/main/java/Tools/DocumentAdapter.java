package Tools;

import ClassifyUtil.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//文档适配器，按二级文件结构将所有数据转化为 List<Document>
public class DocumentAdapter {
    private List<String> tagList; //标签集
    private List<Document> data;   //数据集

    public DocumentAdapter()
    {
        tagList = new ArrayList<>();
        data = new ArrayList<>();
    }

    public void load(String path, float readRatio, boolean isForward)
    {
        File file = new File(path);
        File[] files = file.listFiles();
        System.out.println("提取率：" + readRatio * 100 + "%");
        for(File tmpFile : files)
        {
            String tag = tmpFile.getName();
            tagList.add(tag);
            List<String> tmpList = new ArrayList<>();

            File[] childsFile = tmpFile.listFiles();
            int needCount = (int)(childsFile.length * readRatio);
            System.out.println("类别：" + tag + " 共有：" + childsFile.length + " 篇");
            System.out.println("提取：" + needCount  + "篇");
            int count = 0;
            if(isForward) {
                for (count = 0; count < childsFile.length; count++)
                {
                    if(count > needCount)
                        break;
                    String orgData = DataAdapter.readData(childsFile[count], "UTF-8");
                    data.add(new Document(tag, orgData, false));
                }
            }
            else{
                for(count = childsFile.length-1; count >= 0; count--)
                {
                    if((childsFile.length - count) > needCount)
                        break;
                    String orgData = DataAdapter.readData(childsFile[count], "UTF-8");
                    data.add(new Document(tag, orgData, false));
                }
            }
        }
    }


    public List<String> getTagList()
    {
        return tagList;
    }
    public List<Document> getData()
    {
        return data;
    }

}
