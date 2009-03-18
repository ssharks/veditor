/* behavioral implementation of alarm module */
module alarm_behav(alrm, day2, day1, day0);

	input day2, day1, day0;
	output alrm;

	assign alrm = ~day2 & ~day0  |  ~day1 & ~day0;
endmodule
