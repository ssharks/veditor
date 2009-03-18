module register(D, CLK, Q);
	parameter n = 32;
	input [n-1:0] D;
	input CLK;
	output [n-1:0] Q;
	reg [n-1:0] Q;

	always @(posedge CLK)
		Q = D;
endmodule
