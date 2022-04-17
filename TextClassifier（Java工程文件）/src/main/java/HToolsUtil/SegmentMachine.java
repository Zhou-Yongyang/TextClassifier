package HToolsUtil;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import java.util.List;

//封装HanLP得到的维特比分词器，在python中通过JClass调用
public class SegmentMachine {
    private Segment segment;
    public SegmentMachine()
    {
        segment = HanLP.newSegment("viterbi");
    }
    public String[] segment(String text)
    {
         List<Term> list = segment.seg(text);
         String[] wordArray = new String[list.size()];
         int curIndex = 0;
         for(Term term : list)
         {
            wordArray[curIndex++] = term.word;
         }
         return wordArray;
    }
}
