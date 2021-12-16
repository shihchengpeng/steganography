/*
 * Matrix Embeddingを用いたLSBステガノグラフィ3の実装を行う
 * 入力画像：8ビット濃淡画像
 * 出力画像：8ビット濃淡png画像
 */

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Random;
import java.io.FileWriter;
import java.io.BufferedWriter;
import javax.imageio.ImageIO;


public class LSBstegano3 {
    /*	デバッグメッセージ用フラグ
     *	true の場合，いくつかのメッセージがコンソールに出力される
     */
    static final boolean	DEBUG = true;

    static BufferedImage img = null;

    static int M=-1; // 横サイズ
    static int N=-1; // 縦サイズ
    static int mode; // 埋め込み/抽出
    static int rate; //埋め込みの割合
    static int seed; //埋め込みの位置を決める乱数の種
    static int ImgSize=-1; // 総画素数
    static int[] position=null; //埋め込みの各画素の位置
    static int[] f = null; // 処理後の画素値
    static int[] o = null; // 処理後の画素値
    static int mes_size=0; //メッセージの大きさ
    static String[] g=null; // 処理前の画素値
    static String rfname = null;  //入力画像ファイル名
    static String wfname = null;   //出力画像ファイル名
    static String message = "";  //抽出したメッセージ(文字)
    static String outFileName = null; //生成するファイル名

    public static void main(String[] args) {
        String usage="usage: java SpaceFilteringSample readImage writeImage";

        BufferedImage img = null;
        mode = Integer.parseInt(args[0]);

        //埋め込むのか抽出するのか
        if(mode == 0)
        {
            if (args.length != 5) { //引数の数をチェックできる
                System.out.println("画像引数が指定されていません");
                System.out.println(usage);
                System.exit(1);
            }

            System.out.println("埋め込みが始まる.");

            rfname = args[1];           //入力画像
            rate = Integer.parseInt(args[2]);           //埋め込みの割合
            seed = Integer.parseInt(args[3]);			//乱数生成の種
            wfname = args[4];           //出力画像

            //画像の上書きを防ぐ
            if (rfname.equals(wfname)) {
                System.out.println("rfnameとwfnameが同じです。");
                System.exit(1);
            }

            ReadImage(); // 画像データを読み込む
            genMessage(); //メッセージを読み込む
            genMesPos(seed); //位置を乱数で決める
            Embedding(); // メッセージを埋め込み
            WriteImage(); // 画像データを書き出す
            System.out.println("埋め込み終了");
            checkPixel();
        }
        else if(mode == 1)
        {
            if (args.length != 4) { //引数の数をチェックできる
                System.out.println("画像引数が指定されていません");
                System.out.println(usage);
                System.exit(1);
            }

            System.out.println("抽出が始まる.");

            rfname = args[1];           //入力画像
            seed = Integer.parseInt(args[2]);      //乱数の種
            outFileName = args[3];      //生成するファイル

            ReadImage(); //画像データを読み込む
            getMessage(); //メッセージを取り出す
            WriteText(); //メッセージファイルを出力する
            System.out.println("抽出終了");
        }
    }
    //メッセージを取り出す
    static void getMessage(){
        String comments="メッセージを取り出す";
        if (DEBUG) {
            System.out.println(comments);
        }

        //最初の4ビットをStringの形で繋げる
        String message_length="";
        for(int i=0;i<4;i++)
        {
            while(g[i].length()<8)
                g[i]='0'+g[i];
            message_length += g[i];
        }

        mes_size = Integer.parseInt(message_length, 2); //二進数から数字に変更して、埋め込んだメッセージの長さを取得する
        genMesPos(seed); //埋め込みの位置を決定する

        String input_data = ""; //各画像のLSBをまとめて、一つのStringにする
        for(int i=0;i<mes_size;i++)
        {
            int length = g[position[i]].length();
            input_data += g[position[i]].substring(length-1); //LSBを取得する
        }

        int index = 0;
        while(index < mes_size)
        {
            String tmp = input_data.substring(index,index+7); //まとめたLSB Stringを7ビットごとに一つのhamming codeとして扱う
            String syndrome = genSyndrome(tmp); //シンドロームを計算する
            message += syndrome;  //メッセージを得る
            index += 7;
        }
        System.out.println("抽出したメッセージの長さ: "+message.length());
    }

    //メッセージファイルを作成する
    static void WriteText(){
        try{
            // Create new file
            String content = message;
            File file = new File(outFileName);

            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            // Write in file
            bw.write(content);

            // Close connection
            bw.close();
            if (DEBUG) {
                System.out.println("Output Text File = extracted_message.txt ");
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    //LSBステガノグラフィの手法でメッセージを埋め込む
    static void Embedding() {
        String comments="メッセージを埋め込む";
        if (DEBUG) {
            System.out.println(comments);
        }

        String input_data = "";

        for(int i=0;i<mes_size;i++)  //各画像のLSBをまとめて、一つのStringにする
        {
            int length = g[position[i]].length();
            input_data += g[position[i]].substring(length-1);
        }

        String hamming_data = "";

        int times = 0;
        int index = 0;
        int mes_index = 0;
        while(index < mes_size)
        {
            String tmp = "";
            tmp = input_data.substring(index,index+7);  //まとめたLSB Stringを7ビットごとに一つのhamming codeとして扱う

            String embeddingMes = "";
            for(int i=mes_index;i<mes_index+3;i++)  //埋め込みたいメッセージを3ビットごとに処理する
            {
                embeddingMes += message.charAt(i);
            }

            String syndrome = genSyndrome(tmp);  //シンドロームを計算する
            StringBuilder hammingCode = MatrixEmbedding(tmp,syndrome,embeddingMes);  //シンドロームとメッセージに排他的論理の計算を行って、誤りビットを訂正する
            hamming_data += hammingCode;

            times ++;
            index += 7;
            mes_index += 3;
        }

        for(int i=0;i<mes_size;i++)  //埋め込んだデータをg[]に入れる
        {
            int length = g[position[i]].length();
            g[position[i]] = g[position[i]].substring(0,length-1) + hamming_data.charAt(i);
        }

        System.out.println("繰返し回数：" + times);
        System.out.println("After Steganography");
    }

    static StringBuilder MatrixEmbedding(String data, String syndrome, String embeddingData)
    {
        String tmp = "";
        for(int i=0;i<3;i++)
        {
            if(syndrome.charAt(i)==embeddingData.charAt(i))  //排他的論理の計算
                tmp += '0';
            else
                tmp += '1';
        }

        int index = Integer.parseInt(tmp, 2)-1;
        StringBuilder hammingCode = new StringBuilder(data);

        if(index != -1 && data.charAt(index) == '0')  //誤りビットの訂正
            hammingCode.setCharAt(index, '1');
        else if(index != -1 && data.charAt(index) == '1')
            hammingCode.setCharAt(index, '0');

        return hammingCode;
    }

    static String genSyndrome(String data) //シンドロームの計算
    {
        StringBuilder HammingSyndrome = new StringBuilder("000");
        for(int i=0;i<data.length();i++)
        {
            if(data.charAt(i)=='1')
            {
                String tmp = Integer.toBinaryString(i+1);  //'1'のビットを二進数に変更する
                while(tmp.length()<3) //tmpの中さを確認する
                    tmp = '0' + tmp;
                for(int j=0;j<tmp.length();j++)  //Xorを行う
                {
                    if(HammingSyndrome.charAt(j) == tmp.charAt(j))
                        HammingSyndrome.setCharAt(j,'0');
                    else
                        HammingSyndrome.setCharAt(j,'1');
                }
            }
        }

        return HammingSyndrome.toString();
    }

    //メッセージを読み出す
    static void genMessage(){
        //指定する割合を使って、埋め込みのサイスを計算する
        System.out.println("ImgSize: "+ ImgSize);
        mes_size = (int)(rate*0.01*ImgSize);
        mes_size = mes_size - (mes_size%7);
        message = generateBinaryString(mes_size/7*3); //実際の埋め込みデータ量
        System.out.println("total_size: " + mes_size);
        System.out.println("message_size: " + message.length());

        //メッセージの長さを確認する
        if(message.length()>ImgSize)
        {
            System.out.println("The message is too big to embed.");
            System.exit(1);
        }
        System.out.println("message: "+message);
    }

    static void ReadImage() { // 画像データを読み込む
        try {
            img = ImageIO.read(new File(rfname));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (img != null) {
            if (DEBUG) {
                System.out.println("Input Image = " + rfname);
            }
            M = img.getWidth(); // 幅を設定
            N = img.getHeight(); // 高さを設定
            ImgSize = M * N; // 総画素数を設定
        } else {
            System.out.println("img = null !!!, something is wrong.");
            System.exit(1);
        }

        // 8ビット濃淡画像か否かをチェックする
        if (img.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            System.out.println("GetProperty = " + img.getProperty(rfname));
            System.out.print(img.toString());
            System.exit(1);
        }

        // 画素データを配列fに読み込む
        WritableRaster ras = img.getRaster();
        f = new int[ImgSize]; //画素値で保存する
        g = new String[ImgSize]; //二進数で保存する

        //get data in binary format
        for (int i = 0; i < ImgSize; i++) {
            f[i] = ras.getDataBuffer().getElem(i);
            g[i] = Integer.toBinaryString(ras.getDataBuffer().getElem(i));
            //System.out.println(g[i]);
        }

    }

    //LSBステガノグラフィを行ったファイルを生成する
    static void WriteImage() {
        WritableRaster ras = img.getRaster();
        o = new int[ImgSize];

        String message_length = Integer.toBinaryString(mes_size);
        System.out.println("埋め込んだメッセージ長さ: " + mes_size/7*3);

        //0を使って、message_lengthというstringは32ビットであることを確認する
        while(message_length.length()<32)
            message_length = '0' + message_length;


        //毎8ビットを一つの画像をする　
        //o[0]=message_length(0~7)
        //o[1]=message_length(8~15)
        //o[2]=message_length(16~23)
        //o[3]=message_length(24~31)

        int j=0;
        for(int i=0;i<4;i++)
        {
            o[i] = Integer.parseInt(message_length.substring(j,j+8),2);
            j+=8;
        }

        //o[4]からはLSBステガノグラフィをした画像を入れる
        for(int i=4;i<ImgSize;i++)
        {
            o[i]= Integer.parseInt(g[i], 2);
        }

        //gの値を画像データに設定
        ras.setPixels(0, 0, M, N, o);

        //生成した画像データを書き込む
        try {
            ImageIO.write(img, "png", new File(wfname));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (DEBUG) {
            System.out.println("Output Image = " + wfname);
        }

        System.out.println("out");
    }

    static int findRandom()
    {

        // Generate the random number
        int num = (1 + (int)(Math.random() * 100)) % 2;

        // Return the generated number
        return num;
    }

    // Function to generate a random
    // binary string of length N
    static String generateBinaryString(int N)
    {

        // Stores the empty string
        String S = "";

        // Iterate over the range [0, N - 1]
        for(int i = 0; i < N; i++)
        {

            // Store the random number
            int x = findRandom();

            // Append it to the string
            S = S + String.valueOf(x);
        }

        return S;
    }

    //埋め込みの位置を決める
    static void genMesPos(int seed)
    {
        Random rndFIX = new Random((long)seed); //種を固定する
        position = new int[mes_size];

        for(int i=0;i<mes_size;i++) {
            boolean collosion = false;
            int temp = 0;
            do {
                collosion = false;
                temp = (rndFIX.nextInt(ImgSize - 4)) + 4;
                for(int j=0; j<i; j++){
                    if (position[j] == temp){
                        collosion = true;
                        break;
                    }
                }
            }while(collosion==true);

            position[i] = temp;
        }
    }

    //文字から二進数に変更する
    static String convertStringToBinary(String input) {

        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();
        for (char aChar : chars) {
            result.append(
                    String.format("%8s", Integer.toBinaryString(aChar))   // char -> int, auto-cast
                            .replaceAll(" ", "0")                         // zero pads
            );
        }
        return result.toString();
    }

    //変更したビットの検査を行う
    static void checkPixel()
    {
        int count=0;
        for(int i=0;i<ImgSize;i++)
        {
            if(f[i]!=o[i])
                count++;
        }
        System.out.println("変更した画素数：" + count);
    }

}
