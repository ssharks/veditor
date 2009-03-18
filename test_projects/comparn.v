module comparn(AeqB, AgtB, A, B);
	parameter n = 4;
	input [n-1:0] A, B;
	output AeqB, AgtB;
	integer i;
	reg [n:0] AeqBx, AgtBx;

	assign AeqB = AeqBx[0];
	assign AgtB = AgtBx[0];

	initial begin
		AeqBx[n] = 1;
		AgtBx[n] = 0;
	end

	always @(A or B) 
	begin
		for (i = n-1 ; i >= 0 ; i = i - 1)
		begin
			AeqBx[i] = AeqBx[i+1] & ~(A[i] ^ B[i]);
			AgtBx[i] = AgtBx[i+1] | (AeqBx[i+1] & A[i] & ~B[i]);
		end
	end
endmodule
