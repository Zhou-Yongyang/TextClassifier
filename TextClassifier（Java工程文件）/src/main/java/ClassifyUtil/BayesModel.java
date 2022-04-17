package ClassifyUtil;

import java.io.*;
import java.util.*;

public class BayesModel {
    private final Map<String, Double> classPossible;
    private final Map<String, Map<String, Double>> wordInClassPossible;
    public final List<String> classList;

    private final boolean isModelReady;

    public BayesModel(Map<String, Double> classPossible, Map<String, Map<String, Double>> wordInClassPossible)
    {
        isModelReady = true;
        this.classPossible = classPossible;
        this.wordInClassPossible = wordInClassPossible;
        this.classList = new ArrayList<>();
        this.classList.addAll(classPossible.keySet());
    }
    public Map<String, Double> predict(String text)
    {
        if(!isModelReady) {
            System.out.println("模型未加载完毕");
            return null;
        }
        String[] wordArray = extractTextFeature(text);
        //计算概率
        return calBelongToClassPossible(wordArray);
    }
    public String predictAndBackMax(String text)
    {
        Map<String, Double> prePossible = predict(text);
        String preClass = "";
        double maxPossible = Double.NEGATIVE_INFINITY;
        for(String className : classList)
        {
            if(prePossible.get(className) > maxPossible)
            {
                preClass = className;
                maxPossible = prePossible.get(className);
            }
        }
        return preClass;
    }
    private String[] extractTextFeature(String text)
    {
        String[] wordArray = new String[text.length()-1];
        for(int i = 0; i < text.length() -1; i++)
        {
            wordArray[i] = "";
            wordArray[i] += text.charAt(i);
            wordArray[i] += text.charAt(i+1);
        }
        return wordArray;
    }
    private Map<String, Double> calBelongToClassPossible(String[] wordArray)
    {
        Map<String, Double> prePossible = new HashMap<>();
        for(String className : classList)
        {
            double tmpPossible = classPossible.get(className);
            for(String word : wordArray)
            {
                if(wordInClassPossible.containsKey(word)) {
                    tmpPossible += wordInClassPossible.get(word).get(className);
                }
            }
            prePossible.put(className, tmpPossible);
        }
        return prePossible;
    }


    public void modelOut(String outPath) throws Exception {
        BayesModelInfo modelInfo = new BayesModelInfo(classPossible, wordInClassPossible);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outPath));
        oos.writeObject(modelInfo);
        oos.close();
    }

    public static BayesModel loadModel(String modelPath) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath));
        BayesModelInfo modelInfo =  (BayesModelInfo) ois.readObject();
        BayesModel model = modelInfo.recoverToModel();
        ois.close();
        return model;
    }
}
