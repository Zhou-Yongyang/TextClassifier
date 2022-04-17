package ClassifyUtil;

//中间对象，格式化原数据
public class Document {
    private final String className;
    private String spiltStr;
    private boolean isDivided;

    public Document(String className, String str, boolean isDiveded)
    {
        this.className = className;
        this.spiltStr = str;
        this.isDivided = isDiveded;
    }
    public String getContent()
    {
        if(isDivided) {
            String result;
            result = spiltStr.replace("\t", "");
            return result;
        }
        else
            return spiltStr;
    }

    public String getDocClassName()
    {
        return className;
    }
    public String[] getDocWordArray()
    {
        if(isDivided)
            return spiltStr.split("\t");
        else
            return null;
    }

    public void setDividedSign(boolean isDivided) {
        this.isDivided = isDivided;
    }
    public void setSpiltStr(String spiltStr)
    {
        this.spiltStr = spiltStr;
    }
}
