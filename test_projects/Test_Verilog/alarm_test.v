/* testbench for alarm clock ckt */
module alarm_test;	// no parens if no ports

	reg [2:0] day;
	reg clk;
	wire als, alb;

	alarm_struct alrm1(als, day[2], day[1], day[0]);
	alarm_behav  alrm2(alb, day[2], day[1], day[0]);

	initial begin
		clk = 0;
		day = 3'b000;
	end
		
	always begin
		#10 clk = ~clk;
	end

	always @(posedge clk) begin
		if (als != alb) begin
			$display("Behavioral and structural implementations disagree for day %d", day);
		end 
		else
		begin	
			$display("%d: output matched", day);
		end
		day = day + 1;
		if (day == 3'b000) begin
			$stop;
		end
	end
endmodule
