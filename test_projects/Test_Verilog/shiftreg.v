module shiftreg(EN, in, CLK, Q);
	parameter n = 16;
	input EN;
	input in;
	input CLK;
	output [n-1:0] Q;
	reg [n-1:0] Q;

	// could use a for loop...
	always @(posedge CLK)
	begin
		if (EN) 
			Q[n-1:0] <= {in, Q[n-1:1]};
	end
endmodule
