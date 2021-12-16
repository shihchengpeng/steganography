/*
 * LSBステガノグラフィの実装を行う
 * 入力画像：8ビット濃淡画像
 * 出力画像：8ビット濃淡png画像
 */

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.io.FileWriter;
import java.io.BufferedWriter;

import javax.imageio.ImageIO;


public class LSBstegano {
    /*	デバッグメッセージ用フラグ
     *	true の場合，いくつかのメッセージがコンソールに出力される
     */
    static final boolean	DEBUG = true;
    
    static BufferedImage img = null;
    
    static int M=-1; // 横サイズ
    static int N=-1; // 縦サイズ
    static int mode; // 埋め込み/抽出
    static int ImgSize=-1; // 総画素数
    static String[] g=null; // 処理前の画素値
    static int[] o=null; // 処理後の画素値
    static String[] receive_message; // 抽出したメッセージ(二進数)
    static String rfname = null;  //入力画像ファイル名
    static String mfname = null;  //入力messageファイル名
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
    	if (args.length != 4) { //引数の数をチェックできる
            System.out.println("画像引数が指定されていません");
            System.out.println(usage);
            System.exit(1);
        }

    	System.out.println("埋め込みが始まる.");

    	rfname = args[1];           //入力画像
        mfname = args[2];           //入力message
        wfname = args[3];           //出力画像

        //画像の上書きを防ぐ
        if (rfname.equals(wfname)) {
            System.out.println("rfnameとwfnameが同じです。");
            System.exit(1);
        }

    	ReadImage(); // 画像データを読み込む
		ReadMessage(); //メッセージを読み込む
		Embedding(); // メッセージを埋め込み
		WriteImage(); // 画像データを書き出す
		System.out.println("埋め込み終了");
    }
    else if(mode == 1)
    {
    	if (args.length != 3) { //引数の数をチェックできる
            System.out.println("画像引数が指定されていません");
            System.out.println(usage);
            System.exit(1);
        }

    	System.out.println("抽出が始まる.");

    	rfname = args[1];           //入力画像
        outFileName = args[2];      //生成するファイル

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

		int message_size = Integer.parseInt(message_length, 2); //二進数から数字に変更して、埋め込んだメッセージの長さを取得する
		//System.out.println("test: "+message_length);
		System.out.println("抽出したメッセージの長さ: "+message_size);
		receive_message = new String[message_size/8]; //8ビットで一つの文字を構成するから、合わせてmessage_size/8のサイスが必要

		//配列の初期化
		for(int i=0;i<message_size/8;i++)
		{
			receive_message[i]="";
		}

		int j=0;
		for(int i=4;i<message_size+4;i++)
		{
			int length=g[i].length();
			receive_message[j] += g[i].substring(length-1); //8ビットごとに組み合わせる

			if((i-4)%8==7) //8ビットごとにjというインデックスを更新する
				j++;
		}

		for(int i=0;i<message_size/8;i++)
		{
			message += (char)Integer.parseInt(receive_message[i], 2); //8ビットの二進数を文字に変更する
		}
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

	//グラフの4ビット目から、LSBを置き換える
	for(int i=4;i<message.length()+4;i++)
	{
		int length=g[i].length();
		g[i] = g[i].substring(0,length-1) + message.charAt(i-4);
	}

	System.out.println("After Steganography");
}

	//メッセージを読み出す
    static void ReadMessage(){
    	String data = null;
    	try {
	        data = new String(Files.readAllBytes(Paths.get(mfname)));
		} catch (Exception e) {
	    	e.printStackTrace();
		}
		
		//System.out.println(data);
		message = convertStringToBinary(data); //メッセージを二進数に変更する
		//System.out.println("As binary2: "+message);

		//メッセージの長さを確認する
		if(message.length()>ImgSize)
		{
			System.out.println("The message is too big to embed.");
			System.exit(1);
		}
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
	g = new String[ImgSize];

	//get data in binary format
	for (int i = 0; i < ImgSize; i++) {
	    g[i] = Integer.toBinaryString(ras.getDataBuffer().getElem(i));
	    //System.out.println(g[i]);
	}

    }
    
    //LSBステガノグラフィを行ったファイルを生成する
    static void WriteImage() {
	WritableRaster ras = img.getRaster();
	o = new int[ImgSize];

	String message_length = Integer.toBinaryString(message.length());
	System.out.println("埋め込んだメッセージ長さ: " + message.length());

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
		//System.out.println("m_l: " + o[i]);
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
}
