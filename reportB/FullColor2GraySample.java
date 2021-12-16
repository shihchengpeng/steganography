/*
 * フルカラー画像を濃淡画像に変換する
 */
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.ColorModel;
import java.io.File;

import javax.imageio.ImageIO;

public class FullColor2GraySample {
    public static void main(String[] args) {
	String usage="usage: java FullColor2GraySample readImage writeImage";
        
        if (args.length != 2) { //引数の数をチェックできる
            System.out.println("画像引数が指定されていません");
            System.out.println(usage);
            System.exit(1);
        }
        
        BufferedImage img = null;
        String rfname = args[0];           //入力画像
        String wfname = args[1];           //出力画像

	int M=-1;        //ｘ方向の長さ
	int N=-1;        //ｙ方向の長さ
	int ImgSize=-1;  // 総画素数
	
	//画像の上書きを防ぐ
	if (rfname.equals(wfname)) {
	    System.out.println("rfnameとwfnameが同じです。");
	    System.exit(1);
	}
		
	//画像データを読み込む
	try {
	    img = ImageIO.read(new File(rfname));
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	if (img != null) {
	    M=img.getWidth();		// 横サイズを設定
	    N=img.getHeight();	// 縦サイズを設定
	    ImgSize= M*N;			// 総画素数を設定
	} else {
	    System.out.println("Something is wrong with img.");
	    System.exit(1);
	}
	
	// 画像の色の持ち方をチェック
	if ( BufferedImage.TYPE_3BYTE_BGR != img.getType() ) {
	    System.out.println("対応していないカラーモデルです！("  + rfname +")" );
	    System.exit(1);
	}
	
	int[] pxl = new int[ImgSize];  //画素のすべてのカラー情報が入る
	int[] vR = new int[ImgSize];   //赤成分が入る
	int[] vG = new int[ImgSize];   //緑成分が入る
	int[] vB = new int[ImgSize];   //青成分が入る
	int[] gray = new int[ImgSize];  //濃淡値が入る
	
	// Read RGB array  ： 画素データをpxlに一気に読む込む
	img.getRGB(0, 0, M, N, pxl, 0, M);
	
	//pxlから色成分情報を抜き取る
	for (int i=0; i<ImgSize; i++) {
	    vR[i]=(pxl[i]>>16)&0xff;
	    vG[i]=(pxl[i]>>8)&0xff;
	    vB[i]=(pxl[i]>>0)&0xff;
	}
	
	/* 
	 *濃淡へ変換する処理をここに書く
	 *		  入力: vR, vG, vB　(一次元配列)
	 *             (各色の強さがラスター走査順に格納されている)
	 *
	 *      出力: gray  (一次元配列)
	 *             (輝度値をラスター走査順に格納する）
	 *
	 *　　　使える情報
	 *　　　　　　横サイズ: M
	 *　　　　　　縦サイズ: N
	 *　　　　　　総画素数: ImgSize
	 */
		
	
	for (int i=0; i<ImgSize; i++) {
	    gray[i] = vR[i];
	}
	
	//PNGの濃淡画像として出力
	BufferedImage gimg = new BufferedImage(M, N, BufferedImage.TYPE_BYTE_GRAY);
	WritableRaster ras = gimg.getRaster();
	
	ras.setPixels(0, 0, M, N, gray);
	
	boolean result = false;
	try {
	    result = ImageIO.write(gimg, "png", new File(wfname));
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (result) {
	    System.out.println("Convert [" + rfname + " (FullColor)] to [" + wfname + " (Gray)].");
	}
	
    }
}
