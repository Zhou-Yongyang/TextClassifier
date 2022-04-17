package Tools;
import ClassifyUtil.BayesTrainManager;
import ClassifyUtil.BayesModel;

import java.util.List;


public class Main {
    public static void main(String[] args) {
        //String dataPath = "G:\\语料库\\THUCNews\\THUCNews";
        //String outPath = "D:\\project_file\\NLP\\TextClassifier\\model\\model.txt";
        int argc = args.length;
        if(argc != 4)
            System.out.println("参数数目错误");
        else
        {
            String dataPath = args[0];
            String outPath = args[1];
            int maxFeatureSize = Integer.parseInt(args[2]);
            float ratio = Float.parseFloat(args[3]);
            train(dataPath, outPath, maxFeatureSize, ratio);
            test(dataPath, outPath, maxFeatureSize, ratio);
        }
    }
    public static void train(String dataPath, String outPath, int maxFeatureSize, float ratio)
    {
        BayesModel model = BayesTrainManager.train(dataPath, maxFeatureSize, ratio);
        try {
            model.modelOut(outPath + "\\" + maxFeatureSize + "-" + ratio + ".txt");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void test(String dataPath, String outPath, int maxFeatureSize, float ratio)
    {
        BayesModel model = null;
        try {
            model = BayesModel.loadModel(outPath + "\\" + "Model-" + maxFeatureSize + "-" + ratio + ".txt");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        DocumentAdapter documentAdapter = new DocumentAdapter();
        documentAdapter.load(dataPath, 0.2f, false);
        System.out.println(documentAdapter.getTagList());
        List<String> info = BayesTrainManager.evaluateModel(model, documentAdapter.getData(), documentAdapter.getTagList());
        DataAdapter.writeData(info, outPath + "\\" + "result-" + maxFeatureSize + "-" + ratio + ".txt");
    }
}