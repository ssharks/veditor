module seq1101tb;
	reg CLK, RESET, x;
	wire y, y2;

	seq1101 s0 (x, y, CLK, RESET);
	seq1101_mealy s1 (x, y2, CLK, RESET);

	initial
	begin
		CLK = 1;
		RESET = 0;
		x = 0;
		#5
		RESET = 1;
		#10
		x = 1;
		#10
		x = 0;
		#10
		x = 1;
		#20
		x = 0;
		#10
		x = 1;
		#10
		x = 1;
		#10
		x = 0;
		#10
		x = 1;
		#30
		$finish;
	end

	always
		#5 CLK = ~CLK;

	always @(posedge CLK)
	begin
		$display("%t: x = %b, y = %b, y2 = %b", $time, x, y, y2);
	end
endmodule