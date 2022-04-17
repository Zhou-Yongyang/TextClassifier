package ClassifyUtil;

import java.io.Serializable;
import java.util.Map;


//用于序列化输出和反序列化输入模型
public class BayesModelInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, Double> classPossible;
    private final Map<String, Map<String, Double>> wordInClassPossible;
    private final boolean isAntiSerializable;

    public BayesModelInfo(Map<String, Double> classPossible, Map<String, Map<String, Double>> wordInClassPossible)
    {
        isAntiSerializable = true;
        this.classPossible = classPossible;
        this.wordInClassPossible = wordInClassPossible;
    }
    public BayesModel recoverToModel()
    {
        if(isAntiSerializable)
            return new BayesModel(classPossible, wordInClassPossible);
        else
            return null;
    }
}
