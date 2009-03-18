// Two slashes starts a comment to end-of-line
/* C style multi-line comments 
 * are also allowed
 */

/* structural implementation of alarm ckt */
module alarm_struct(alrm, day2, day1, day0);  //don't forget the ';'

	input day2, day1;
	input day0;
	output alrm;
	wire nd2, nd1, nd0;
	wire p1, p2;

	// structural code lists components and connections
	not(nd2, day2);
	not(nd1, day1);
	not(nd0, day0);
	and(p1, nd2, nd0);
	and(p2, nd1, nd0);
	or(alrm, p1, p2);
endmodule
