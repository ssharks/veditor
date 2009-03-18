//
// Positive edge triggered flip-flop with asynchronous clear
//
module regclr(D, CLK, CLR_, Q);
	parameter n = 32;
	input [n-1:0] D;
	input CLK;
	input CLR_;
	output [n-1:0] Q;
	reg [n-1:0] Q;

	// cannot mix level sensitive and edge triggered
	// sensitivity lists
	always @(posedge CLK or negedge CLR_)
		if (CLR_ == 1)
			Q = D;
		else
			Q = 0;
endmodule
