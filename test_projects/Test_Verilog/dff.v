//
// Positive edge-triggered flip-flop
//
module dff(D, CLK, Q);
	input D, CLK;
	output Q;
	reg Q;

	always @(posedge CLK)
		Q = D;
endmodule

//
// Negative edge-triggered flip-flop
//
module dff_neg(D, CLK, Q);
	input D, CLK;
	output Q;
	reg Q;

	always @(negedge CLK)
		Q = D;
endmodule
