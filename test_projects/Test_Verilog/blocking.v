module blocking(CLK);
	reg A, B, X;
	reg C, D;
	input CLK;

	always @(posedge CLK)
	begin
		A = X;
		B = A;
		// After rising clock edge, both A and B have X
	end

	always @(posedge CLK)
	begin
		C <= X;
		D <= C;
		// After rising clock edge, C has X,
		// D has original value of C
	end
endmodule
