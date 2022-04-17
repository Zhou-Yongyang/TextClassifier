package Tools;

//进度条工具
public class ProgressBar {
    public static void printProgressPercent(double now, double total)
    {
        int totalLenth = 30;
        for (int i = 0; i < totalLenth*3; i++) {
            System.out.print("\b");
        }
        System.out.print("Training: ");
        double percent =  now / total;
        System.out.print("[");
        for (int i = 0; i < totalLenth * percent; i++) {
            System.out.print(">");
        }
        for(int i = 0; i < totalLenth * (1-percent); i++)
        {
            System.out.print(" ");
        }
        System.out.print("]");
        System.out.printf(" %.2f ",percent*100);
        System.out.print("%");
    }
}
