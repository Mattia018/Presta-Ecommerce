import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RecommendationResponse } from "../../models/RecommendationResponse";
import { Router } from '@angular/router';
import { ProductService } from 'src/app/services/product.service';

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent {

  isOpen = false; 
  userQuery: string = ''; 
  responseData: RecommendationResponse | null = null; 
  loading = false;

  constructor(private http: HttpClient, private router: Router, private productservice : ProductService) {}

  toggleChat() {
    this.isOpen = !this.isOpen; 
    if (!this.isOpen) {
      this.responseData = null; 
    }
  }

  sendQuery() {
    if (!this.userQuery.trim()) return; 
    this.loading = true;

    
    this.productservice.getRecommendation(this.userQuery)
      .subscribe(response => {
        response.recommendationText = this.formatTextWithLineBreaks(response.recommendationText);
          this.responseData = response;
          this.loading = false;
      
      }, error => {
        console.error('Errore API:', error);
        this.loading = false;
      });

    this.userQuery = ''; 
  }

  formatTextWithLineBreaks(text: string): string {
    let formattedText = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    formattedText = formattedText.replace(/\n/g, '<br>');
    return formattedText;
  }

  deleChat(){
    this.responseData = null
  }

  goToProductDetails(productId: number): void {
    this.router.navigate(['/shop-details', productId]);
  }

}
