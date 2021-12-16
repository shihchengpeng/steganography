/*
 * ハミング符号の誤り訂正
 * 入力数値：受信語
 * 出力数値：誤りビット、訂正語
 */

import java.lang.*;

public class Make74HammingErrorCorrection
{
    public static void main(String[] args) 
    {
    	String data = args[0];	//受信語を読み込む
    	System.out.println("受信語：" + data);
    	String HammingSyndrome = genHammingSyndrome(data); //シンドロームを取得する
    	genErrorCorrection(data,HammingSyndrome); //受信語とシンドロームにより、誤りビットを見つけ、訂正する
	}

	static String genHammingSyndrome(String data)
	{
		//排他的論理の計算
		int c1 = Character.getNumericValue(data.charAt(0)) ^ Character.getNumericValue(data.charAt(1)) ^ Character.getNumericValue(data.charAt(2)) ^ Character.getNumericValue(data.charAt(4));
		int c2 = Character.getNumericValue(data.charAt(0)) ^ Character.getNumericValue(data.charAt(1)) ^ Character.getNumericValue(data.charAt(3)) ^ Character.getNumericValue(data.charAt(5));
		int c3 = Character.getNumericValue(data.charAt(0)) ^ Character.getNumericValue(data.charAt(2)) ^ Character.getNumericValue(data.charAt(3)) ^ Character.getNumericValue(data.charAt(6));

		System.out.println("シンドローム：" + c1 + "," + c2 + "," + c3);
		String HammingSyndrome = "" + c1 + c2 + c3;
		return HammingSyndrome;
	}

	static void genErrorCorrection(String data, String HammingSyndrome)
	{
		StringBuilder message = new StringBuilder(data);
		int errorBit = 8 - Integer.parseInt(HammingSyndrome,2); //二進数のシンドロームから誤りビットを特定する
		if(errorBit == 8)	//シンドロームは000の場合、誤りなし
			System.out.println("誤りなし");
		else
		{
			if(data.charAt(errorBit-1) == '0') //誤りビットを訂正する
				message.setCharAt(errorBit-1,'1');
			else
				message.setCharAt(errorBit-1,'0');

			System.out.println("Hの" + errorBit + "列目と一致します");
			System.out.println(errorBit + "ビット目が誤っています");
			System.out.println("訂正語：" + message);
		}
	}
}

