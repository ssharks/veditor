module dlatch(D, EN, Q);
	input D, EN;
	output Q;
	reg Q;

	always @(D or EN)
	if (EN)
		Q = D;
	// missing else implies Q does not change,
	// i.e., it is stored in a flip-flop
endmodule