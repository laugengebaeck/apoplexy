#include <avr/io.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>

#define F_CPU 8000000ul
#include <util/delay.h>
#define BAUD 9600ul
#define UBBRVAL 51

void init(void);
void u_putch(uint8_t x);
void u_puts(char *s);
void sendCurrentVoltage(void);
void delay(uint16_t millisec);

int main(void) {	
	init();
	while(1) {
		sendCurrentVoltage();
		delay(500);
	}
	return 0;
}

void init(void) {
	ADMUX = 0x00;
	ADCSRA = (1 << ADEN) | (1 << ADPS2) | (1 << ADPS1);
	ADCSRA |= (1<<ADSC);
	while (ADCSRA & (1<<ADSC));
	(void) ADC;
	
	UBRR0H = UBBRVAL >> 8;
	UBRR0L = UBBRVAL & 0xFF;
	UCSR0B |= (1 << TXEN0) | (1 << RXEN0);
	UCSR0C |= (1 << UCSZ01) | (1 << UCSZ00);
	(void) UDR0;
}

void u_putch (uint8_t x) {
	while(!(UCSR0A & (1 << UDRE0)));
	UDR0 = x;
}

void u_puts (char *s) {
	while (*s) {
		u_putch(*s++);
	}
}

void sendCurrentVoltage(void) {
	ADCSRA |= (1 << ADSC);
	while (ADCSRA & (1 << ADSC));
	uint16_t out = ADC;
	double voltage = (out/1024.0) * 4.5;
	
	char str[10];
	sprintf(str, "%.3f \r\n", voltage);
	u_puts(str);
}

void delay(uint16_t millisec) {
	while(millisec--) {
		_delay_ms(1);
	}
}


