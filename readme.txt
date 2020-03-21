# Merkle-Hellman Knapsack

Algorithm:
= list of sizes, (x1, . . . , xn) to be super-increasing 
= select modulus where p > super-increasing sum
= select multiplier where a < p-1
= public key (ti) = a * si mod p
= secret key (x, a, p)

Receiver decrypts using a^−1  ,found using extended
Eucledean, i.e. y = a^-1 x ti mod p

Receiver solves the instance I = (b,y)  of the 
subset sum problem and obtains the plaintext.
