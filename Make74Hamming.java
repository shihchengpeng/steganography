/*
 * ハミング符号の生成
 * 入力数値：ハミング通報
 * 出力数値：ハミング符号
 */

import java.lang.*;

public class Make74Hamming 
{
    public static void main(String[] args) 
    {
    	String data = args[0];	//通報を読み込む
    	String HammingCode = genHammingCode(data); //ハミング符号の生成
    	System.out.println("通報：" + data);
    	System.out.println("ハミング符号：" + HammingCode);
	}

	static String genHammingCode(String data)
	{
		//排他的論理の計算
		int x1 = Character.getNumericValue(data.charAt(1)) ^ Character.getNumericValue(data.charAt(2)) ^ Character.getNumericValue(data.charAt(3));
		int x2 = Character.getNumericValue(data.charAt(0)) ^ Character.getNumericValue(data.charAt(2)) ^ Character.getNumericValue(data.charAt(3));
		int x3 = Character.getNumericValue(data.charAt(0)) ^ Character.getNumericValue(data.charAt(1)) ^ Character.getNumericValue(data.charAt(3));

		//通報と検査ビットを組み合わせる
		String HammingCode = data + x1 + x2 + x3;
		return HammingCode;
	}
}

