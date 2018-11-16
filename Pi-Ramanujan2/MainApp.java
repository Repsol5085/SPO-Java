import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Vector;
import java.io.File;

public class MainApp {

    static BigDecimal result = BigDecimal.ZERO;
    static BigDecimal[] factorials;
    static BigDecimal[] eightEightTwoSqareToTheNth;

    public static BigDecimal fact(int n) {
        if(n<=1)
            return BigDecimal.valueOf(1);
        if(factorials[n]!=null)
            return factorials[n];
        factorials[n]=BigDecimal.valueOf(n).multiply(fact(n-1));
        return factorials[n];
    }

    public static BigDecimal eightEightTwoSqaredToTheNth(int n) {
        if (n == 0) return BigDecimal.valueOf(1);
        if (eightEightTwoSqareToTheNth[n] != null)
            return eightEightTwoSqareToTheNth[n];
        eightEightTwoSqareToTheNth[n] = BigDecimal.valueOf(882*882).multiply(eightEightTwoSqaredToTheNth(n - 1));
        return eightEightTwoSqareToTheNth[n];
    }

    public static void savetofile(String fileName, BigDecimal pi) {
        try {
            File file = new File(fileName);
            if (!file.exists())
                file.createNewFile();
            PrintWriter pw = new PrintWriter(file);
            pw.println(pi.toString());
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Runnable appRun(final int start, final int end, int numThreads, boolean qm) {
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                long startThread = System.currentTimeMillis();
                if(!qm)
                    System.out.println(Thread.currentThread().getName() + " has been started.");

                BigDecimal pi = BigDecimal.ZERO;

                for (int i = start; i < end; i+=numThreads) {

                    BigDecimal top = BigDecimal.valueOf(i % 2 == 0 ? 1 : -1)
                            .multiply(fact(4*i))
                            .multiply(BigDecimal.valueOf(1123+21460*i));
                    BigDecimal h8 = eightEightTwoSqaredToTheNth(i);
                    BigDecimal h2 = BigDecimal.valueOf(4).pow(i);
                    BigDecimal facti=fact(i);
                    BigDecimal bot = (h2.multiply(facti)).pow(4)
                            .multiply(h8);
                    BigDecimal mid = top.divide(bot,new MathContext(10000, RoundingMode.HALF_UP));
                    pi = pi.add(mid);
                }

                assignResult(pi);
                if(!qm) {
                    long endThread = System.currentTimeMillis();
                    System.out.println(Thread.currentThread().getName()+" has been stopped.");
                    System.out.println(Thread.currentThread().getName()+": "+(endThread-startThread)+" ms worktime.");
                }
            }
        };
        return runner;
    }

    public static synchronized void assignResult(BigDecimal pi){
        result = result.add(pi);
    }

    public static void main(String[] args) throws InterruptedException {
        Vector<String> args1 = new Vector<>(Arrays.asList(args));
        int sizeofN = 2000;
        int numThreads = 2;
        boolean quietMode = false;
        String fileName = "pi.txt";

        if(args1.contains("-p"))
            sizeofN=Integer.parseInt(args1.get(args1.indexOf("-p")+1));
        if(args1.contains("-t"))
            numThreads=Integer.parseInt(args1.get(args1.indexOf("-t")+1));
        else if(args1.contains("-task"))
            numThreads=Integer.parseInt(args1.get(args1.indexOf("-task")+1));
        if(args1.contains("-q"))
            quietMode=true;
        if(args1.contains("-o"))
            fileName = args1.get(args1.indexOf("-o") + 1);

        factorials = new BigDecimal[4*(sizeofN+1)];
        eightEightTwoSqareToTheNth = new BigDecimal[sizeofN+1];
        Thread[] threads = new Thread[numThreads];
        long start1 = System.currentTimeMillis();

        final int sn = sizeofN;
        new Thread(new Runnable() {
            @Override
            public void run() {
                BigDecimal current = BigDecimal.valueOf(1);
                for (int i = 2; i < 4 * sn; i++) {
                    current = current.multiply(BigDecimal.valueOf(i));
                    factorials[i] = current;
                }
            }
        }).start();

        for(int i = 0; i<numThreads; i++)
            threads[i]=new Thread(appRun(i,sizeofN,numThreads,quietMode));
        for(int i =0; i < threads.length;i++)
            threads[i].start();
        for(int i =0; i < threads.length;i++)
            threads[i].join();

        result = BigDecimal.valueOf(882*4).divide(result, new MathContext(10000, RoundingMode.HALF_UP));
        System.out.println("pi = " + result);
        long stop = System.currentTimeMillis();
        long diff = stop - start1;
        System.out.println("Total work time: "+diff + " ms");
        savetofile(fileName,result);
    }

}