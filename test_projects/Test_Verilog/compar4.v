module compar4(AeqB, AgtB, A, B);
	input [3:0] A;
	input [3:0] B;
	output AeqB, AgtB;

	compar1 c0 (AeqB, AgtB, A[0], B[0], AeqB1, AgtB1);
	compar1 c1 (AeqB1, AgtB1, A[1], B[1], AeqB2, AgtB2);
	compar1 c2 (AeqB2, AgtB2, A[2], B[2], AeqB3, AgtB3);
	compar1 c3 (AeqB3, AgtB3, A[3], B[3], 1'b1, 1'b0);
endmodule