module counter(enable, load, d, clk, q, cout);
	parameter n = 8;
	input enable, load;
	input [n-1:0] d;
	input clk;
	output [n-1:0] q;
	reg [n-1:0] q;
	output cout;
	wire [n-1:0] a0, x0, din;

	and a[n-1:0] (a0, {a0[n-2:0],enable}, q[n-1:0]);
	xor x[n-1:0] (x0, {a0[n-2:0],enable}, q[n-1:0]);
	assign din = load ? d : x0;
	assign cout = a0[n-1];

	always @(posedge clk)
	begin
		q <= din;
	end
endmodule
