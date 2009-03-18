`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: University of Kentucky
// Engineer:  William R. Dieter
// 
// Create Date:    22:20:27 01/18/2006 
// Design Name:    RPS
// Module Name:    ee281tut 
// Project Name:   Rock, Paper, Scissors
// Target Devices: XC3S200
// Tool versions:  ISE 8.1
// Description:    This circuit handles input and display for approximately
//		   one half of a game of Rock, Paper, Scissors.  The slide
//		   switches are used for inputs (00 = rock, 01 = paper, 
//		   10 = scissors, 11 = no-op) and two buttons must be
//		   pressed to see the result.
//
// Dependencies: 
//
// Revision: 
// Revision 0.01 - File Created
// Additional Comments: 
//
//////////////////////////////////////////////////////////////////////////////////
module ee281tut(HAND, GO1, GO2, SSEG, DISP, AN);
    input [1:0] HAND;
    input GO1, GO2;
    output [2:0] AN;
    output [7:0] SSEG;
    reg    [7:0] SSEG;
    output DISP;

	// Turn off three of the 7 segment displays
	assign AN = 3'b111;
	// Turn on the first 7 segment display
	assign DISP = ~(GO1 & GO2);
	
	// Control the segments of the display
	always @(HAND)
	begin
		case (HAND)
		2'b00: SSEG = 7'b10101111;	// 00 = rock
		2'b01: SSEG = 7'b10001100;	// 01 = Paper
		2'b10: SSEG = 7'b10010010;	// 10 = Scissors
		default: SSEG = 7'b11111111;	// Everything else = no display
		endcase
	end
endmodule
