class LoaderService {
    constructor() {
        this.fullScreenLoader = null;
        this.init();
    }

    init() {
        this.fullScreenLoader = document.getElementById('fullScreenLoader');
        
        if (!this.fullScreenLoader) {
            this.fullScreenLoader = document.createElement('div');
            this.fullScreenLoader.id = 'fullScreenLoader';
            this.fullScreenLoader.className = 'loader-overlay';
            this.fullScreenLoader.innerHTML = `
                <i class="bi bi-camera-reels film-reel-spinner"></i>
                <span class="loader-text">Ładowanie...</span>
            `;
            document.body.appendChild(this.fullScreenLoader);
        }
    }

    showFullScreen(text = 'Ładowanie...') {
        if (!this.fullScreenLoader) {
            this.init();
        }
        
        const textEl = this.fullScreenLoader.querySelector('.loader-text');
        if (textEl) {
            textEl.textContent = text;
        }
        
        this.fullScreenLoader.classList.add('show');
    }

    hideFullScreen() {
        if (this.fullScreenLoader) {
            this.fullScreenLoader.classList.remove('show');
        }
    }

    showButton(buttonIdOrElement, loadingText = null) {
        const button = typeof buttonIdOrElement === 'string' 
            ? document.getElementById(buttonIdOrElement)
            : buttonIdOrElement;
        
        if (!button) return;
        
        let loader = button.querySelector('.btn-loader');
        let originalText = button.querySelector('.btn-text');
        
        if (!loader) {
            loader = document.createElement('span');
            loader.className = 'btn-loader';
            loader.innerHTML = `
                <i class="bi bi-camera-reels film-reel-spinner"></i>
                <span>${loadingText || 'Przetwarzanie...'}</span>
            `;
        } else {
            const textSpan = loader.querySelector('span');
            if (textSpan && loadingText) {
                textSpan.textContent = loadingText;
            }
        }
        
        if (!originalText) {
            originalText = document.createElement('span');
            originalText.className = 'btn-text';
            originalText.textContent = button.textContent.trim();
            button.insertBefore(originalText, button.firstChild);
        }
        
        if (originalText) originalText.style.display = 'none';
        loader.classList.add('show');
        if (!button.contains(loader)) {
            button.insertBefore(loader, button.firstChild);
        }
        
        button.disabled = true;
    }

    hideButton(buttonIdOrElement) {
        const button = typeof buttonIdOrElement === 'string' 
            ? document.getElementById(buttonIdOrElement)
            : buttonIdOrElement;
        
        if (!button) return;
        
        const loader = button.querySelector('.btn-loader');
        const originalText = button.querySelector('.btn-text');
        
        if (loader) {
            loader.classList.remove('show');
        }
        
        if (originalText) {
            originalText.style.display = 'inline';
        }
        
        button.disabled = false;
    }

    showInline(containerIdOrElement, text = 'Ładowanie...') {
        const container = typeof containerIdOrElement === 'string'
            ? document.getElementById(containerIdOrElement)
            : containerIdOrElement;
        
        if (!container) return null;
        
        const loader = document.createElement('div');
        loader.className = 'container-loader';
        loader.innerHTML = `
            <i class="bi bi-camera-reels film-reel-spinner"></i>
            <span class="loader-text">${text}</span>
        `;
        
        container.innerHTML = '';
        container.appendChild(loader);
        
        return loader;
    }

    createSmall() {
        const loader = document.createElement('span');
        loader.className = 'small-loader';
        loader.innerHTML = '<i class="bi bi-camera-reels film-reel-spinner"></i>';
        return loader;
    }
}

const loaderService = new LoaderService();

if (typeof module !== 'undefined' && module.exports) {
    module.exports = LoaderService;
}

