class ModalService {
    constructor() {
        this.modalContainer = null;
        this.currentModal = null;
        this.init();
    }

    init() {
        this.modalContainer = document.getElementById('modal-container');
        
        if (!this.modalContainer) {
            this.modalContainer = document.createElement('div');
            this.modalContainer.id = 'modal-container';
            document.body.appendChild(this.modalContainer);
        }
    }

    confirm(title, message, onConfirm, onCancel = null, options = {}) {
        const {
            confirmText = 'Potwierdź',
            cancelText = 'Anuluj',
            type = 'warning',
            icon = null
        } = options;

        return this.show({
            title,
            message,
            type,
            icon,
            buttons: [
                {
                    text: cancelText,
                    class: 'btn-secondary',
                    action: () => {
                        this.hide();
                        if (onCancel) onCancel();
                    }
                },
                {
                    text: confirmText,
                    class: type === 'danger' ? 'btn-danger' : 'btn-primary',
                    action: () => {
                        this.hide();
                        if (onConfirm) onConfirm();
                    }
                }
            ]
        });
    }

    alert(title, message, onClose = null, options = {}) {
        const {
            buttonText = 'OK',
            type = 'info',
            icon = null
        } = options;

        return this.show({
            title,
            message,
            type,
            icon,
            buttons: [
                {
                    text: buttonText,
                    class: 'btn-primary',
                    action: () => {
                        this.hide();
                        if (onClose) onClose();
                    }
                }
            ]
        });
    }

    showSuccess(title, message, details = null, actions = []) {
        const buttons = actions.length > 0 
            ? actions 
            : [
                {
                    text: 'OK',
                    class: 'btn-success',
                    action: () => this.hide()
                }
            ];

        let messageHtml = this.escapeHtml(message);
        
        if (details) {
            messageHtml += '<div class="modal-details mt-3">';
            if (typeof details === 'object') {
                for (const [key, value] of Object.entries(details)) {
                    messageHtml += `<div class="detail-item"><strong>${this.escapeHtml(key)}:</strong> ${this.escapeHtml(value)}</div>`;
                }
            } else {
                messageHtml += `<div>${this.escapeHtml(details)}</div>`;
            }
            messageHtml += '</div>';
        }

        return this.show({
            title,
            message: messageHtml,
            type: 'success',
            icon: '<i class="bi bi-check-circle-fill"></i>',
            buttons
        });
    }

    show(config) {
        if (this.currentModal) {
            this.hide();
        }

        const modal = this.createModal(config);
        this.modalContainer.appendChild(modal);
        this.currentModal = modal;

        setTimeout(() => {
            modal.classList.add('show');
            const backdrop = modal.querySelector('.modal-backdrop');
            if (backdrop) backdrop.classList.add('show');
        }, 10);

        this.escapeHandler = (e) => {
            if (e.key === 'Escape' && this.currentModal === modal) {
                this.hide();
            }
        };
        document.addEventListener('keydown', this.escapeHandler);

        return modal;
    }

    createModal(config) {
        const modal = document.createElement('div');
        modal.className = 'custom-modal';
        
        const {
            title,
            message,
            type = 'info',
            icon = null,
            buttons = []
        } = config;

        const defaultIcons = {
            success: '<i class="bi bi-check-circle-fill"></i>',
            error: '<i class="bi bi-x-circle-fill"></i>',
            warning: '<i class="bi bi-exclamation-triangle-fill"></i>',
            info: '<i class="bi bi-info-circle-fill"></i>'
        };

        const displayIcon = icon || defaultIcons[type] || defaultIcons.info;

        modal.innerHTML = `
            <div class="modal-backdrop"></div>
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header modal-${type}">
                        <div class="modal-icon">${displayIcon}</div>
                        <h5 class="modal-title">${this.escapeHtml(title)}</h5>
                        <button type="button" class="btn-close" aria-label="Zamknij">
                            <i class="bi bi-x"></i>
                        </button>
                    </div>
                    <div class="modal-body">
                        <div class="modal-message">${message}</div>
                    </div>
                    <div class="modal-footer">
                        ${buttons.map((btn, index) => `
                            <button type="button" class="btn ${btn.class}" data-action-index="${index}">
                                ${this.escapeHtml(btn.text)}
                            </button>
                        `).join('')}
                    </div>
                </div>
            </div>
        `;

        buttons.forEach((btn, index) => {
            const button = modal.querySelector(`[data-action-index="${index}"]`);
            if (button) {
                button.addEventListener('click', () => {
                    btn.action();
                });
            }
        });

        const closeBtn = modal.querySelector('.btn-close');
        closeBtn.addEventListener('click', () => {
            this.hide();
        });

        const backdrop = modal.querySelector('.modal-backdrop');
        backdrop.addEventListener('click', (e) => {
            if (e.target === backdrop) {
                this.hide();
            }
        });

        return modal;
    }

    hide() {
        if (!this.currentModal) {
            return;
        }

        this.currentModal.classList.remove('show');
        const backdrop = this.currentModal.querySelector('.modal-backdrop');
        if (backdrop) backdrop.classList.remove('show');

        setTimeout(() => {
            if (this.currentModal && this.currentModal.parentNode) {
                this.currentModal.parentNode.removeChild(this.currentModal);
            }
            this.currentModal = null;
        }, 300);

        if (this.escapeHandler) {
            document.removeEventListener('keydown', this.escapeHandler);
            this.escapeHandler = null;
        }
    }

    escapeHtml(text) {
        if (typeof text !== 'string') return text;
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

const modalService = new ModalService();

if (typeof module !== 'undefined' && module.exports) {
    module.exports = ModalService;
}

