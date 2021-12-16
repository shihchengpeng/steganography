/*
 * 空間フィルタリングを行う
 * 入力画像：8ビット濃淡png画像
 * 出力画像：8ビット濃淡png画像
 */

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;


public class SpaceFilteringSample {
    /*	デバッグメッセージ用フラグ
     *	true の場合，いくつかのメッセージがコンソールに出力される
     */
    static final boolean	DEBUG = true;
    
    static BufferedImage img = null;
    
    static int M=-1; // 横サイズ
    static int N=-1; // 縦サイズ
    static int ImgSize=-1; // 総画素数
    static int[] f=null; // 原画像の画素値
    static int[] g=null; // 処理後の画素値
    static String rfname = null;  //入力画像ファイル名
    static String wfname = null;   //出力画像ファイル名
    
    public static void main(String[] args) {
	String usage="usage: java SpaceFilteringSample readImage writeImage";
        
        if (args.length != 2) { //引数の数をチェックできる
            System.out.println("画像引数が指定されていません");
            System.out.println(usage);
            System.exit(1);
        }
        
        BufferedImage img = null;
        rfname = args[0];           //入力画像
        wfname = args[1];           //出力画像
	
	//画像の上書きを防ぐ
        if (rfname.equals(wfname)) {
            System.out.println("rfnameとwfnameが同じです。");
            System.exit(1);
        }

	ReadImage(); // 画像データを読み込む
	
	Filtering(); // フィルタリング
	
	WriteImage(); // 画像データを書き出す
	
	System.out.println("Done.");
    }
    
    static void Filtering() {
	// 一次元配列fには濃淡画像の画素値がラスタ走査順に格納されている
	String comments="画素値を反転します";
	
	if (DEBUG) {
	    System.out.println(comments);
	}
	
	for (int i = 0; i < ImgSize; i++) {
	    g[i] = 255 - f[i];
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
	f = new int[ImgSize];
	g = new int[ImgSize];
	for (int i = 0; i < ImgSize; i++) {
	    f[i] = ras.getDataBuffer().getElem(i);
	}
    }
    
    static void WriteImage() {
	WritableRaster ras = img.getRaster();

	//gの値を画像データに設定
	ras.setPixels(0, 0, M, N, g);

	//生成した画像データを書き込む
	try {
	    ImageIO.write(img, "png", new File(wfname));
	} catch (Exception e) {
	    e.printStackTrace();
	}
	if (DEBUG) {
	    System.out.println("Output Image = " + wfname);
	}
    }
}
