/*
 * ハミング符号のシンドロームの生成
 * 入力数値：受信語
 * 出力数値：シンドローム
 */

import java.lang.*;

public class Make74HammingSyndrome 
{
    public static void main(String[] args) 
    {
    	String data = args[0];	//受信語を読み込む
    	String HammingSyndrome = genHammingSyndrome(data); //シンドロームを取得する
    	System.out.println("受信語：" + data);
    	System.out.println("シンドローム：" + HammingSyndrome);
	}

	static String genHammingSyndrome(String data)
	{
		//排他的論理の計算
		int c1 = Character.getNumericValue(data.charAt(0)) ^ Character.getNumericValue(data.charAt(1)) ^ Character.getNumericValue(data.charAt(2)) ^ Character.getNumericValue(data.charAt(4));
		int c2 = Character.getNumericValue(data.charAt(0)) ^ Character.getNumericValue(data.charAt(1)) ^ Character.getNumericValue(data.charAt(3)) ^ Character.getNumericValue(data.charAt(5));
		int c3 = Character.getNumericValue(data.charAt(0)) ^ Character.getNumericValue(data.charAt(2)) ^ Character.getNumericValue(data.charAt(3)) ^ Character.getNumericValue(data.charAt(6));

		String HammingSyndrome = "" + c1 + "," + c2 + "," + c3;
		return HammingSyndrome;
	}
}

