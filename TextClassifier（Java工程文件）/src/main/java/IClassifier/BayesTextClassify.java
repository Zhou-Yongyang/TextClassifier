package IClassifier;

import ClassifyUtil.BayesModel;
import Tools.DataAdapter;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

//分类器
public class BayesTextClassify {
    private final BayesModel model;
    private List<String> classNameList;

    public BayesTextClassify(BayesModel model)
    {
        this.model = model;
        this.classNameList = model.classList;
    }
    public BayesTextClassify(String modelPath) throws Exception {
        model = BayesModel.loadModel(modelPath);
        this.classNameList = model.classList;
    }

    //文本分类预测
    public String predictByText(String text)
    {
        Map<String, Double> preMap = predictEveryClassPossible(text);
        return getMaxLogPossibleEntry(preMap).getKey();
    }
    //文件分类预测
    public String predictByFile(String filePath, String encoding)
    {
        Map<String, Double> preMap = predictEveryClassPossible(filePath, encoding);
        return getMaxLogPossibleEntry(preMap).getKey();
    }

    public Map<String, Double> predictEveryClassPossible(String text)
    {
        Map<String, Double> preMap = model.predict(text);
        return preMap;
    }
    public Map<String, Double> predictEveryClassPossible(String filePath, String encoding)
    {
        String text = DataAdapter.readData(filePath, encoding);
        Map<String, Double> preMap = model.predict(text);
        return preMap;
    }

    private Map.Entry<String, Double> getMaxLogPossibleEntry(Map<String, Double> possibleMap)
    {
        String preClass = "";
        double maxPossible = Double.NEGATIVE_INFINITY;
        for(String className : classNameList)
        {
            if(possibleMap.get(className) > maxPossible)
            {
                preClass = className;
                maxPossible = possibleMap.get(className);
            }
        }
        return new AbstractMap.SimpleEntry<>(preClass, maxPossible);
    }
}
