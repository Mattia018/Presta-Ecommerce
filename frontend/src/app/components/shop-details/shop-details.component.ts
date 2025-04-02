import { Component,Renderer2, ElementRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Product } from 'src/app/models/product';
import { AuthService } from 'src/app/services/auth.service';
import { ProductService } from 'src/app/services/product.service';
import { Router } from '@angular/router';
import { CartEventService } from 'src/app/services/cart-event.service';
import { CartService } from 'src/app/services/cart.service';

@Component({
  selector: 'app-shop-details',
  templateUrl: './shop-details.component.html',
  styleUrls: ['./shop-details.component.scss']
})
export class ShopDetailsComponent {


  product: Product| null=null ;
  selectedImage: string | undefined;
  images: string[] = [];
  showZoom = false; 
  overlayPosition = { x: 0, y: 0 };
  zoomFactor = 4; 
  isAutoScrollActive = true; 
  private autoScrollInterval: any;

  
  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private authService: AuthService,
    private cartService: CartService,
    private router: Router,
    private renderer: Renderer2, private el: ElementRef
  ) {}

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (productId) {
      this.productService.getProductById(+productId).subscribe((product: Product) => {
        this.product = product;

        this.images = [
          this.product.imgResources,
          this.product.imgResources2,
          this.product.imgResources3
        ].filter(img => img);

      });
    }

    
    this.startAutoScroll();


  }
  
  // Metodo add Product to Cart
  addToCart(productId: number,event: MouseEvent): void {


    // Animazione Carrello
    const userId = this.authService.getCurrentUserId();
    if (userId) {

      
    const button = event.target as HTMLElement;
    const cartIcon = document.querySelector('.cart-icon') as HTMLElement;

    const buttonRect = button.getBoundingClientRect();
    const cartRect = cartIcon.getBoundingClientRect();

    const flyIcon = this.renderer.createElement('div');
    this.renderer.addClass(flyIcon, 'fly-to-cart-icon');
    this.renderer.appendChild(flyIcon, this.renderer.createText('🛒'));
    this.renderer.appendChild(document.body, flyIcon);

    this.renderer.setStyle(flyIcon, 'position', 'fixed');
    this.renderer.setStyle(flyIcon, 'left', `${buttonRect.left + buttonRect.width / 2}px`);
    this.renderer.setStyle(flyIcon, 'top', `${buttonRect.top + buttonRect.height / 2}px`);

    setTimeout(() => {
      this.renderer.setStyle(flyIcon, 'transition', 'all 0.5s ease-in-out');
      this.renderer.setStyle(flyIcon, 'left', `${cartRect.left + cartRect.width / 2}px`);
      this.renderer.setStyle(flyIcon, 'top', `${cartRect.top + cartRect.height / 2}px`);
      this.renderer.setStyle(flyIcon, 'opacity', '0');
      this.renderer.setStyle(flyIcon, 'transform', 'scale(0.5)');

      setTimeout(() => {
        this.renderer.removeChild(document.body, flyIcon);
      }, 500);
    }, 0);
  

      // Metodo add Product
    
      this.cartService.addProductToCart(userId, productId, 1).subscribe({
        next: () => {
          
        },
        error: (err) => {
          console.error('Errore durante l\'aggiunta al carrello:', err);
          alert('Si è verificato un errore durante l\'aggiunta al carrello.');
        }
      });
    } else {
      // Se l'utente non è loggato, reindirizzalo alla pagina di login
      this.router.navigate(['/login']);
    }
  }


  // Features Carosello

  selectImage(imageUrl: string): void {
    this.selectedImage = imageUrl;
  }

  startAutoScroll(): void {
    let currentIndex = 0;

    this.autoScrollInterval = setInterval(() => {
      if (this.isAutoScrollActive) {
        currentIndex = (currentIndex + 1) % this.images.length; 
        this.selectedImage = this.images[currentIndex]; 
      }
    }, 3000); 
  }

  stopAutoScroll(): void {
    clearInterval(this.autoScrollInterval); 
  }

  onMouseMove(event: MouseEvent): void {
    if (!this.showZoom) return;

    
    this.isAutoScrollActive = false;

    const imgElement = event.target as HTMLImageElement;
    const canvas = document.getElementById('zoom-canvas') as HTMLCanvasElement;
    const ctx = canvas.getContext('2d');

    if (!imgElement || !ctx) return;

    ctx.imageSmoothingEnabled = true;
    ctx.imageSmoothingQuality = 'high';

    const imgWidth = imgElement.width;
    const imgHeight = imgElement.height;
    const canvasWidth = canvas.width;
    const canvasHeight = canvas.height;

    
    const rect = imgElement.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;

    
    this.overlayPosition.x = mouseX - 25; 
    this.overlayPosition.y = mouseY - 25; 

    
    const sx = (mouseX / imgWidth) * imgElement.naturalWidth - canvasWidth / (2 * this.zoomFactor);
    const sy = (mouseY / imgHeight) * imgElement.naturalHeight - canvasHeight / (2 * this.zoomFactor);

    
    ctx.clearRect(0, 0, canvasWidth, canvasHeight);
    ctx.drawImage(
      imgElement,
      sx,
      sy,
      canvasWidth / this.zoomFactor,
      canvasHeight / this.zoomFactor,
      0,
      0,
      canvasWidth,
      canvasHeight
    );
  }

  onMouseLeave(): void {
    this.isAutoScrollActive = true;
  }

}
