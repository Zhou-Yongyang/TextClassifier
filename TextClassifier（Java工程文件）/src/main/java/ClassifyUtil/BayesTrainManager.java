package ClassifyUtil;

import Tools.ProgressBar;
import Tools.DocumentAdapter;

import java.util.*;

public class BayesTrainManager {

    private static List<String> tagList;
    private static Map<String, Map<String, Integer>> wordClassFrequency;    //一个词在各个类别的词频
    private static Map<String, Integer>classTotalFrequency;                  //一个类别下所有词的词频
    private static Map<String, Integer> documentClassFrequency;             //一个类别下文档的个数

    //分词线程内部类，用于多线程提取加工文档
    private static class SegmentThread extends Thread
    {
        public List<Document> dataList;
        public String className;

        SegmentThread(String className, List<Document> orgData)
        {
            this.className = className;
            this.dataList = orgData;
        }
        @Override
        public void run() {
            int count = 0;
            for(Document data : dataList)
            {
                StringBuilder divideStr = new StringBuilder();
                String orgStr = data.getContent();
                for(int i = 0; i < orgStr.length() - 1; i++)
                {
                    divideStr.append(orgStr.charAt(i));
                    divideStr.append(orgStr.charAt(i+1));
                    if(i != orgStr.length() - 2)
                    {
                        divideStr.append("\t");
                    }
                }
                data.setDividedSign(true);
                data.setSpiltStr(divideStr.toString());
                dataList.set(count, data);
                count++;
                if(count % (dataList.size() / 5) == 0)
                    System.out.println("分词-类别：" + className + " 完成率：" + (float)count/dataList.size());
            }
        }
    }

    //训练
    public static BayesModel train(String dataPath, int maxFeatureSize, float readRatio)
    {
        //导入数据到数据集
        DocumentAdapter documentAdapter = new DocumentAdapter();
        documentAdapter.load(dataPath, readRatio, true);
        //标签集
        tagList = documentAdapter.getTagList();

        //数据划分
        List<Document> allData = documentAdapter.getData();
        int trainDataSize = allData.size();
        System.out.println("标签集：" + tagList);
        //切分处理
        mutilThread_makeDocumentContentSegmentWordList(allData);

        //开始统计词频
        wordClassFrequency = new HashMap<>();       //词表
        documentClassFrequency = new HashMap<>();   //类别文档信息
        classTotalFrequency = new HashMap<>();       //类别总词频

        //频次统计
        calDocumentFreqcy(allData);
        //这里需要分词。。。
        calWordInClassFrequency(allData);

        //特征过滤
        k2FilterFeature(trainDataSize, maxFeatureSize);
        claClassTotalFrequency();

        //计算概率
        //1、各个类别的基础概率
        Map<String, Double> classPossible = new HashMap<>();
        calClassPossible(classPossible, trainDataSize);
        //2、某个词在某一个类别下的概率
        Map<String, Map<String, Double>> wordInClassPossible = new HashMap<>();
        calFeaturePossible(wordInClassPossible);

        return new BayesModel(classPossible, wordInClassPossible);
    }

    //计算类别先验概率
    private static void calClassPossible(Map<String, Double> classPossible, int trainDataSize)
    {
        System.out.println("\n计算类别概率");
        long totalDocCount = trainDataSize;
        //先验概率
        for(String className : tagList)
        {
            double curPossible = (double)documentClassFrequency.get(className) / (double)totalDocCount;
            classPossible.put(className, Math.log(curPossible));
            System.out.println("类别：" + className + " 的概率为： " + (curPossible*100) + "%");
        }
    }

    //特征概率计算
    private static void calFeaturePossible(Map<String, Map<String,Double>> wordInClassPossible)
    {
        System.out.println("\n计算特征概率");
        int count = 0;
        for(String curWord : wordClassFrequency.keySet())
        {
            count++;
            if(count % (wordClassFrequency.size() / 33) == 0)
                ProgressBar.printProgressPercent(count, wordClassFrequency.size());
            Map<String, Double> possibleMap = new HashMap<>();
            for(String className : tagList)
            {
                double possible = (double) (wordClassFrequency.get(curWord).get(className) + 1) / (classTotalFrequency.get(className) + wordClassFrequency.size());
                possibleMap.put(className, Math.log(possible));
            }
            wordInClassPossible.put(curWord, possibleMap);
        }
        System.out.println("\n特征概率计算完毕");
    }

    //划分数据集为训练集测试集。
    public static void divideData(List<Document> allData, List<Document> trainData, List<Document> testData, float ratio)
    {
        System.out.println("开始划分数据");
        Random random = new Random(1);
        for(Document doc : allData)
        {
            if(random.nextFloat() < ratio) {
                if (trainData != null)
                    trainData.add(doc);
            }
            else {
                if(testData != null)
                    testData.add(doc);
            }
        }
        System.out.println("划分数据结束");
    }

    //文档数目频次计算
    private static void calDocumentFreqcy(List<Document> docList)
    {
        documentClassFrequency.clear();
        for(Document curDoc : docList) {
            //类别文档频次
            String curClassName = curDoc.getDocClassName();
            if (!documentClassFrequency.containsKey(curClassName))
                documentClassFrequency.put(curClassName, 0);
            documentClassFrequency.put(curClassName, documentClassFrequency.get(curClassName) + 1);
        }
        for(String className : tagList)
        {
            System.out.println("[类别]： " + className  + "  共：" + documentClassFrequency.get(className) + " 篇");
        }
    }
    //多线程处理
    private static void mutilThread_makeDocumentContentSegmentWordList(List<Document> documents)
    {
        Map<String, List<Document>> segmentMap = new HashMap<>();
        for(Document curDoc : documents)
        {
            String curClassName = curDoc.getDocClassName();
            if(!segmentMap.containsKey(curClassName))
                segmentMap.put(curClassName, new ArrayList<>());
            segmentMap.get(curClassName).add(curDoc);
        }
        List<SegmentThread> segmentThreadPool = new ArrayList<>();
        int count = 0;
        for(String segmentTag : tagList)
        {
            segmentThreadPool.add(new SegmentThread(segmentTag, segmentMap.get(segmentTag)));
            System.out.println("创建线程: " + segmentTag + segmentThreadPool.get(count));
            count++;
        }

        //创建线程
        System.out.println("多线程分词开始----------------------------");
        for(SegmentThread segmentThread : segmentThreadPool)
        {
            segmentThread.start();
        }
        try {
            for (SegmentThread segmentThread : segmentThreadPool) {
                segmentThread.join();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("多线程分词结束---------------------------");
        segmentThreadPool.clear();
        System.gc();
    }
    //计算所有词的频次情况
    private static void calWordInClassFrequency(List<Document> docList)
    {
        System.out.println("\n计算词频");
        wordClassFrequency.clear();
        int count = 0;
        int totalCount = docList.size();
        for(int i = totalCount - 1; i >=0; i--)
        {
            Document curDoc = docList.get(i);
            if(count % (int)(totalCount / 50) == 0)
                ProgressBar.printProgressPercent(count, totalCount);
            count++;
            String curClassName = curDoc.getDocClassName();
            String[] wordArray = curDoc.getDocWordArray();
            for(String curWord : wordArray)
            {
                Map<String, Integer> wordCountMap;
                if(!wordClassFrequency.containsKey(curWord)) {
                    wordClassFrequency.put(curWord, new HashMap<>());
                     wordCountMap = wordClassFrequency.get(curWord);
                    for (String tmpClassName : tagList) {
                        wordCountMap.put(tmpClassName, 0);
                    }
                }
                wordCountMap = wordClassFrequency.get(curWord);
                wordCountMap.put(curClassName, wordCountMap.get(curClassName) + 1);
            }
            docList.remove(i);
        }
        System.out.println("\n词频计算结束");
    }
    //过滤后统计每一类的特征词频
    private static void claClassTotalFrequency()
    {
        classTotalFrequency.clear();
        for(String curWord : wordClassFrequency.keySet())
        {
            Map<String, Integer> wordCountMap = wordClassFrequency.get(curWord);
            for(String className : wordCountMap.keySet())
            {
                if(!classTotalFrequency.containsKey(className))
                {
                    classTotalFrequency.put(className, 0);
                }
                int tmpCount = classTotalFrequency.get(className);
                classTotalFrequency.put(className, tmpCount + wordCountMap.get(className));
            }
        }
    }
    //卡方过滤
    private static void k2FilterFeature(int trainDataSize, int maxFeatureSize)
    {
        System.out.println("k2过滤特征");
        Map<String, Double> wordK2Map = new HashMap<>();

        int count = 0;
        for(String curWord : wordClassFrequency.keySet())
        {
            count++;
            if(count % (int)(wordClassFrequency.size() / 5.0f) == 0)
            ProgressBar.printProgressPercent(count, wordClassFrequency.size());
            Map<String, Integer> wordFreq = wordClassFrequency.get(curWord);
            //该词的总频次
            int wordTotalFreq  = 0;
            for(String classNmae : tagList)
            {
                wordTotalFreq += wordFreq.get(classNmae);
            }
            //文档的总数量
            int docTotalCount = trainDataSize;
            double n0dot = docTotalCount - wordTotalFreq;
            //开始计算
            for(String className : tagList)
            {
                double n11 = wordFreq.get(className);
                double n01 = documentClassFrequency.get(className) - n11;
                double n00 = n0dot - n01;
                double n10 = wordTotalFreq - n11;
                double curK2 = (double)docTotalCount * Math.pow(n11*n00 - n10*n01,2.0D) / ((n11 + n01) *(n11 + n10) * (n10 + n00) * (n01 + n00));
                if(curK2 >= 10.83)
                {
                    Double tmpK2 = wordK2Map.get(curWord);
                    if(tmpK2 == null || tmpK2 < curK2)
                        wordK2Map.put(curWord, curK2);
                }
            }
        }

        chooseEffectiveFeature(wordK2Map, maxFeatureSize);

        //重新筛出特征：
        Map<String, Map<String,Integer>> tmpWordCountMap = new HashMap<>();
        for(String curWord : wordK2Map.keySet())
        {
            tmpWordCountMap.put(curWord, wordClassFrequency.get(curWord));
        }
        System.out.println("原特征数量：" + wordClassFrequency.size());
        System.out.println("挑选特征数量：" + tmpWordCountMap.size());
        System.out.println("占比：" + (double)tmpWordCountMap.size() / wordClassFrequency.size());
        wordClassFrequency = tmpWordCountMap;
    }
    //选择有效数目的特征
    private static void chooseEffectiveFeature(Map<String, Double> wordK2Map, int maxSize)
    {
        //挑选
        if(wordK2Map.size() > maxSize)
        {
            MaxHeap<Map.Entry <String, Double>> maxHeap = new MaxHeap<>(maxSize, Map.Entry.comparingByValue());
            for(Map.Entry<String, Double> entry : wordK2Map.entrySet())
            {
                maxHeap.add(entry);
            }
            wordK2Map.clear();
            for(Map.Entry<String, Double> entry : maxHeap)
            {
                wordK2Map.put(entry.getKey(), entry.getValue());
            }
        }
        //System.out.println(wordK2Map.keySet());
    }
    //模型评估器
    public static List<String> evaluateModel(BayesModel model, List<Document> docList, List<String> classNameList)
    {
        List<String> info = new ArrayList<>();
        System.out.println("开始模型评测");

        int totalCount = 0;
        int correctCount = 0;

        Map<String, Integer> TP = new HashMap<>();
        Map<String, Integer> FP = new HashMap<>();
        Map<String, Integer> FN = new HashMap<>();

        for(String tag :classNameList)
        {
            TP.put(tag, 0);
            FP.put(tag, 0);
            FN.put(tag, 0);
        }

        for(Document doc : docList)
        {
            if(totalCount % (int)(docList.size() / 20.0f) == 0)
                ProgressBar.printProgressPercent(totalCount, docList.size());
            totalCount++;
            String preClass = model.predictAndBackMax(doc.getContent());
            String actClass = doc.getDocClassName();
            //统计情况
            for(String className : classNameList)
            {
                if(preClass.equals(className))   //预测为该类
                {
                    if(preClass.equals(actClass)) //正类预测为正类
                    {
                        TP.put(className, TP.get(className) + 1);
                        correctCount++;
                    }
                    else                           //把负类预测为正类
                    {
                        FP.put(className,FP.get(className) + 1);
                    }
                }
                else
                {
                    if(actClass.equals(className)) //把正类预测为负类
                    {
                        FN.put(className, FN.get(className) + 1);
                    }
                }
            }
        }

        int Parg = 0,Rarg = 1 , F1arg = 2;
        float TPtotal = 0, FPtotal = 0, FNtotal = 0;
        float[] macroAvg = new float[3];   //宏平均
        float[] microAvg = new float[3];   //微平均
        Map<String, float[]> classEvaluate = new HashMap<>();
        for(String tag : classNameList)
        {
            float curTP = TP.get(tag);
            float curFP = FP.get(tag);
            float curFN = FN.get(tag);
            //微平均
            TPtotal += curTP;
            FPtotal += curFP;
            FNtotal += curFN;

            //该类统计
            classEvaluate.put(tag, new float[3]);
            float curP = curTP / (curFP + curTP);
            float curR = curTP / (curTP + curFN);
            float curF1 = (2*curP*curR) / (curP + curR);
            float[] curClass = classEvaluate.get(tag);
            curClass[Parg] = curP;
            curClass[Rarg] = curR;
            curClass[F1arg] = curF1;
            classEvaluate.put(tag, curClass);

            //宏平均
            macroAvg[Parg] += curP;
            macroAvg[Rarg] += curR;
        }
        microAvg[Parg] = TPtotal / (TPtotal + FPtotal);
        microAvg[Rarg] = TPtotal / (TPtotal + FNtotal);
        microAvg[F1arg] = 2*microAvg[Parg]*microAvg[Rarg] / (microAvg[Parg] + microAvg[Rarg]);

        macroAvg[Parg] = macroAvg[Parg] / classNameList.size();
        macroAvg[Rarg] = macroAvg[Rarg] / classNameList.size();
        macroAvg[F1arg] = 2*macroAvg[Parg]*macroAvg[Rarg] / (macroAvg[Parg] + macroAvg[Rarg]);

        System.out.println();
        for(String tag : classNameList)
        {
            float[] prf = classEvaluate.get(tag);
            System.out.println("单一类别：" + tag);
            System.out.println("P: " + prf[Parg]);
            System.out.println("R: " + prf[Rarg]);
            System.out.println("F1: " + prf[F1arg]);
            info.add("-------------------------");
            info.add("单一类别：" + tag);
            info.add("P: " + prf[Parg]);
            info.add("R: " + prf[Rarg]);
            info.add("F1: " + prf[F1arg]);
            info.add("-------------------------");
        }

        System.out.println("----------------------------------------------");
        info.add("----------------------------------------------");
        System.out.println("\n准确率：" + (float)correctCount / totalCount);
        info.add("\n准确率：" + (float)correctCount / totalCount);
        System.out.println("宏平均评测：");
        info.add("宏平均评测：");
        System.out.println("P: " + macroAvg[Parg]);
        info.add("P: " + macroAvg[Parg]);
        System.out.println("R: " + macroAvg[Rarg]);
        info.add("R: " + macroAvg[Rarg]);
        System.out.println("F1: " + macroAvg[F1arg]);
        info.add("F1: " + macroAvg[F1arg]);
        System.out.println("微平均评测：");
        info.add("微平均评测：");
        System.out.println("P: " + microAvg[Parg]);
        info.add("P: " + microAvg[Parg]);
        System.out.println("R: " + microAvg[Rarg]);
        info.add("R: " + microAvg[Rarg]);
        System.out.println("F1: " + microAvg[F1arg]);
        info.add("F1: " + microAvg[F1arg]);
        System.out.println("----------------------------------------------");
        info.add("----------------------------------------------");
        return info;
    }
}
