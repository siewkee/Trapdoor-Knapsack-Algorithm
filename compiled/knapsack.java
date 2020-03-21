/*
Name: Hung Siew Kee
Student ID: 5986606

parallels@parallels-Parallels-Virtual-Platform:~$ java -versionopenjdk version "11.0.3" 2019-04-16OpenJDK Runtime Environment (build 11.0.3+7-Ubuntu-1ubuntu218.04.1)OpenJDK 64-Bit Server VM (build 11.0.3+7-Ubuntu-1ubuntu218.04.1, mixed mode, sharing)
*/

import java.util.*;
import java.math.BigInteger;
import java.security.SecureRandom;

public class knapsack
{
	static BigInteger one = new BigInteger("1");
	
	public static void main (String [] args)
	{
		Scanner console = new Scanner (System.in); 
		
		// user input: knapsack size
		System.out.print("Enter size of knapsack: ");
		int knapsack_size = console.nextInt();
		
		// user input: knapsack value = ai {a1 a2 a3... an}
		int ai[] = new int[knapsack_size];
		int knapsack_sum = secretKey(ai, knapsack_size);
		
		// user input: modulus = m, where m > (1-n)E ai
		BigInteger m_bigint = modulus (knapsack_sum);
		
		// user input: multiplier = w, where gcd(w, m) = 1
		BigInteger w_bigint = multiplier(m_bigint);
		
		// public key = bi {b1 b2 b3 ... bn} where bi = w * ai mod m
		// store bi in array
		int bi[] = new int[knapsack_size];
		publicKey(ai, bi, knapsack_size, w_bigint, m_bigint);
		
		// user input: mode of operation (encrypt or decrypt)
		console.nextLine();
		System.out.print("\nEnter mode of operation (Encrypt / Decrypt): ");
		String op_mode = console.nextLine();
		System.out.println();
		
		/*** 
			Character Padding:
			- program process character's ASCII code range from 32 - 126
			- max 7 bits representation of each possible character
			- total length(multiple of knapsack size) = 7 char bits + zeros + 111 (binary form of 7, 3 bits)
		***/
			
		// establish total bit length for each character
	 	int char_bit_len = char_bitLength(knapsack_size);
		
		if (op_mode.equalsIgnoreCase("encrypt"))
		{
			// user input: message
			System.out.print("\nEnter message for encryption: ");
			String msg = console.nextLine();

			// store message in char array
			char msg_char[] = msg.toCharArray();
			
			//padding and store into array
			String char_bin_padding[] = new String[msg_char.length];
			char_padding(msg_char, char_bit_len, char_bin_padding);
		
			/*****
			Encryption (can be done with more than 1 word)
			- loop string in segments (multiple of knapsack size)
			- sum knapsack value if bit = 1
			- each character has multiple segments of sum knapsack (assuming knapsack size is smaller than binary length)
			- knapsack values of entire message stored in 2D array, each row = 1 character, each column = 1 knapsack sum of character represented by row
			*****/	
			
			int ciphertext[][] = new int[msg_char.length][char_bit_len/knapsack_size];
			encryption(ciphertext, char_bin_padding, knapsack_size, char_bit_len, bi);
			
			System.out.print("\nCiphertext: ");
			for (int j = 0; j < ciphertext.length; j++)
			{
				for (int i : ciphertext[j])
					System.out.print(i + " ");
			}
			System.out.println();
		
		}
		else
		{
			/*****	
			Decryption (can only decrypt one word at a time)
			- loop array to extract all knapsack sum
			- multiply inverse value
			- identify binary for each segment
			- add all segments together to form original padded binary string for one character
			*****/
			
			//user input: cipher text
			int cipher_int[];
			String cipher_str[];
			boolean cipher_size_check = true;
			
			do
			{
				System.out.print("\nEnter cipher text: ");
				String input = console.nextLine();
				
				cipher_str = input.split("\\s", 0);
				if (cipher_str.length % (char_bit_len/knapsack_size) != 0)
				{
					System.out.println("\nCiphertext does not match with knapsack size. Please try again!");
					cipher_size_check = false;
				}			
						
			}while(cipher_size_check == false);
			
			cipher_int = new int [cipher_str.length];
		
			System.out.println();
			for (int j = 0; j < cipher_int.length; j++)
				cipher_int[j] = Integer.parseInt(cipher_str[j]);

			String plaintext_bin_padded[] = new String[cipher_int.length/(char_bit_len/knapsack_size)];
			decryption(m_bigint, w_bigint, knapsack_size, cipher_int, ai, plaintext_bin_padded, char_bit_len);
			
			/*****
			Reverse padding
			- read last 3 bit to establish original number of bits (i.e 7)
			- extract binary substring of 0 - 7  = ASCII code
			- convert to char and store in array
			****/
			char plaintext_char[] = new char[plaintext_bin_padded.length];
			reverse_char_padding(plaintext_char, plaintext_bin_padded, char_bit_len);
			
			System.out.print("Plaintext: ");
			for (char c: plaintext_char)
				System.out.print(c);
			
			System.out.println();
		}
		
		
	}
	
	public static int secretKey(int ai[], int size)
	{
		// store ai in array
		// check if all vaues are super-increasing
		Scanner console = new Scanner (System.in);
		boolean super_increase_check = false;
		int super_increase_sum = 0;
		
		for (int i = 0; i < size; i++)
		{
			super_increase_check = false;
			
			do
			{
				System.out.print("Enter super-increasing value a(" + i + "): ");
				ai[i] = console.nextInt();
				if (ai[i] > super_increase_sum)
				{
					super_increase_check = true;
					super_increase_sum += ai[i];
				}	
				else
					System.out.println("Value is not super-increasing. Please try again!");
					
			}while(super_increase_check == false);
		}
		
		return super_increase_sum;
	}
	
	public static BigInteger multiplier(BigInteger m_bigint)
	{
		Scanner console = new Scanner (System.in);
		
		boolean gcd_check = false;
		BigInteger w_bigint = one; 
		
		do
		{
			System.out.print("\nEnter multiplier(w) value: ");
			int w = console.nextInt();
			w_bigint = BigInteger.valueOf(w);
			BigInteger gcd = m_bigint.gcd(w_bigint);
			
			if (gcd.equals(one))
				gcd_check = true;
			else
				System.out.println("GCD of m and w is not 1. Please try again!");
				
		}while(gcd_check == false);
		
		return w_bigint;
	}
	
	public static BigInteger modulus (int knapsack_sum)
	{
		Scanner console = new Scanner(System.in);
		
		int m = 0;
		while (m < knapsack_sum)
		{
			System.out.print("\nEnter modulus(m) value: ");
			m = console.nextInt();
			
			if (m < knapsack_sum)
				System.out.println("Value is not larger than sum of knapsack. Please try again!");
		}
		
		BigInteger m_bigint = BigInteger.valueOf(m);
		
		return m_bigint;
	}
	
	public static void publicKey(int ai[], int bi[], int size, BigInteger w_bigint, BigInteger m_bigint)
	{
		System.out.print("\nPublic key: ");
		for (int i = 0; i < size; i++)
		{
			BigInteger ai_temp = BigInteger.valueOf(ai[i]);
			BigInteger bi_temp = (w_bigint.multiply(ai_temp)).mod(m_bigint);
			bi[i] = bi_temp.intValue();
			
			if (i == size - 1)
				System.out.println(bi[i]);
			else
				System.out.print(bi[i] + ", ");
		}
	}
	
	public static int char_bitLength(int knapsack_size)
	{
		// if knapsack_size < 10, msg_length = next multiple of n where multiple > 10
		// if knapsack_size > 10, msg_length = knapsack size
		int char_bit_length = 0;
		if (knapsack_size % 10 != 0 && knapsack_size < 10)
		{
			int i = 0;
			while (char_bit_length < 10)
				char_bit_length += knapsack_size;
		}
		else
			char_bit_length = knapsack_size;
		System.out.println("char_bit_length: " + char_bit_length);
		
		return char_bit_length;
	
	}
	
	public static void char_padding(char msg_char[], int char_bit_len, String char_bin_padding[])
	{
		for (int i = 0; i < msg_char.length; i++)
		{ 
			// convert each char to binary ASCII value
			String char_bin = Integer.toBinaryString((int)msg_char[i]);

			// add leading zeros to make up 7 bits if necessary
			if (char_bin.length() != 7)
			{
				int add_zeros = 7 - char_bin.length();
				for (int j = 0; j < add_zeros; j++)
					char_bin = "0" + char_bin;
			}
			
			// add (char_bit_len - 10) number of zeros to char
			for (int k = 0; k < char_bit_len-10; k++)
				char_bin += "0";
				
			// add 111 at the end
			char_bin += "111";
			
			//store in array
			char_bin_padding[i] = char_bin;
		}
	}
	
	public static void encryption(int ciphertext[][], String char_bin_padding[], int knapsack_size, int char_bit_len, int bi[])
	{
		//loop array for string of padded binary
		for (int i = 0; i < char_bin_padding.length; i++)
		{
			//convert string of padded binary to integer value
			int char_pad_bin2int = Integer.parseInt(char_bin_padding[i], 2);
			int bit_count = 0;
			
			//loop 2D array //each row = one char // each column = one segment of char row is representing
			for (int j = 0; j < char_bit_len/knapsack_size; j++)
			{
				for (int k = 0; k < knapsack_size; k++)
				{
					//calculate knapsack sum for individual segment
					if (char_bin_padding[i].charAt(bit_count) == '1')
						ciphertext[i][j] += bi[k];
					bit_count++;
				}
			}	
		}
	}
	
	public static void decryption(BigInteger m_bigint, BigInteger w_bigint, int knapsack_size, int cipher_int[], int ai[], String plaintext_bin_padded[], int char_bit_len)
	{
		//inverse w
		BigInteger w_inverse = w_bigint.modInverse(m_bigint);
		
		String char_bin = "";
		String segment_bin = "";
		int plaintext_bin_padded_count = 0;
		int cipher_int_count = 0;
		
		//loop array for every char where each char has (char_bit_len/knapsack_size) segments
		for (int j = 0; j < cipher_int.length; j += char_bit_len/knapsack_size)
		{
			char_bin = "";
			
			//loop array for every segment of one char
			for (int i = 0; i < char_bit_len/knapsack_size; i++)
			{
				segment_bin = "";
				
				BigInteger Ti_bigint = BigInteger.valueOf(cipher_int[cipher_int_count]);
					
				// Ti * w^-1 mod m = knapsack_sum				
				BigInteger knap_sum = (Ti_bigint.multiply(w_inverse));
				knap_sum = knap_sum.mod(m_bigint);
				int knap_sum_int = knap_sum.intValue();
					
				int knap_index = knapsack_size -1;
				
				//loop array for every knapsack value to compare with knapsack sum for each segment of one char
				for (int w = 0; w < knapsack_size; w++)
				{
					if (knap_sum_int >= ai[knap_index])
					{
						segment_bin = 1 + segment_bin;
						knap_sum_int -= ai[knap_index];
					}
					else
						segment_bin = 0 + segment_bin;
						knap_index--;
				}
				
				cipher_int_count++;
				char_bin += segment_bin;
			}
			
			plaintext_bin_padded[plaintext_bin_padded_count] = char_bin;	
			plaintext_bin_padded_count++;	
			
		}		
	}
	
	public static void reverse_char_padding(char plaintext_char[], String plaintext_bin_padded[], int char_bit_len)
	{
		for (int i = 0; i < plaintext_char.length; i++)
		{
			//establish original bit length by reading last 3 bit of padded string
			int length = char_bit_len;
			int ori_bit_len = Integer.parseInt(plaintext_bin_padded[i].substring(length-3), 2);
			
			//extract binary substring that represents the char
			String plaintext_bin = plaintext_bin_padded[i].substring(0, ori_bit_len);
			
			//ASCII value of char
			int plaintext_int = Integer.parseInt(plaintext_bin, 2); 
			
			//store char in array
			plaintext_char[i] = (char) plaintext_int;
		}
	}
}


