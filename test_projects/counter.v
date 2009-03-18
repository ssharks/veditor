module counter(load, p, clk, q);
	parameter n = 16;
	input load;
	input [n-1:0] p;
	input clk;
	output [n-1:0] q;
	reg [n-1:0] q;

	always @(posedge clk)
		if (load)
			q = p;
		else
			q = q + 1;
endmodule
	