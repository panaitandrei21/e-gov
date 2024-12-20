import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import {NgbInputDatepicker} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule} from "@angular/forms";
import {HttpClient, HttpClientModule, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NgbInputDatepicker, CommonModule, FormsModule, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  fullName: string = '';
  cnp: string = '';
  address: string = '';
  passportType: number | null = null;  // Inițial, nicio opțiune selectată
  processingFee: number = 0;
  totalAmount: number = 0;
  constructor(private http: HttpClient) { }
  private apiUrl = 'http://localhost:8080/api/payments/submit';
  calculateFees() {
    if (this.passportType !== null) {
      this.processingFee = parseFloat((this.passportType * 0.05).toFixed(2));
      this.totalAmount = Number(this.passportType) + this.processingFee;
    } else {
      this.processingFee = 0;
      this.totalAmount = 0;
    }
  }

  isFormValid(): boolean {
    return this.fullName.trim() !== '' && this.cnp.trim() !== '' && this.address.trim() !== '' && this.passportType !== null;
  }

  onSubmit() {
    if (this.isFormValid()) {
      const paymentData = {
        fullName: this.fullName,
        cnp: this.cnp,
        address: this.address,
        passportType: this.passportType,
        processingFee: this.processingFee,
        totalAmount: this.totalAmount
      };

      this.submitPayment(paymentData).subscribe(
        (response: Blob) => {
          // Use FileReader to handle the blob data and download it
          const fileReader = new FileReader();
          fileReader.onload = (e: any) => {
            const link = document.createElement('a');
            link.href = e.target.result;
            link.download = 'factura-pasaport.pdf';
            document.body.appendChild(link);  // Required for Firefox
            link.click();
            document.body.removeChild(link);  // Clean up the DOM
          };
          fileReader.readAsDataURL(response);  // Convert blob to base64 URL
        },
        (error) => {
          console.error('Error occurred:', error);
          alert('A apărut o eroare la trimiterea plății.');
        }
      );
    } else {
      alert('Te rog să completezi toate câmpurile formularului!');
    }
  }

  submitPayment(paymentData: any): Observable<any> {
    return this.http.post(this.apiUrl, paymentData, {
      responseType: 'blob' as 'json',  // Primim răspunsul ca un blob (fișier)
      headers: new HttpHeaders().append('Accept', 'application/pdf')
    });
  }
  downloadReport() {
    this.generateReport().subscribe(
      (response: Blob) => {
        const url = window.URL.createObjectURL(response);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'report.pdf';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      (error) => {
        console.error('Error generating report:', error);
      }
    );
  }
  generateReport(): Observable<Blob> {
    return this.http.get('http://localhost:8080/api/payments/generate-pdf', { responseType: 'blob' });
  }

}
