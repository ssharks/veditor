module compar1(AeqB, AgtB, A, B, AeqBin, AgtBin);
	input A, B, AeqBin, AgtBin;
	output AeqB, AgtB;
	wire eq0, gt0;

	// A == B when A and B are same, and all more significant bits are same
	and (AeqB, AeqBin, eq0);
	xnor (eq0, A, B);

	// A > B when A == 1 and B == 0 and more significant bits were same
	// or if more significant bits where A > B
	or(AgtB, AgtBin, gt0);
	and(gt0, AeqBin, A, ~B);
endmodule
	